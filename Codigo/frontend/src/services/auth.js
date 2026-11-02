import API_URL from './api';

async function readResponse(response) {
  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    const error = new Error(data.erro || data.message || 'Não foi possível concluir a operação.');
    error.status = response.status;
    throw error;
  }

  return data;
}

export async function getRecaptchaSiteKey() {
  const response = await fetch(`${API_URL}/api/auth/recaptcha-site-key`);
  const data = await readResponse(response);

  if (!data.siteKey) {
    throw new Error('A chave do reCAPTCHA não foi configurada no servidor.');
  }

  return data.siteKey;
}

export async function submitAuthRequest(endpoint, payload, captchaToken) {
  const headers = {
    'Content-Type': 'application/json',
  };

  if (captchaToken) {
    headers['X-Recaptcha-Response'] = captchaToken;
  }

  const response = await fetch(`${API_URL}/api/auth/${endpoint}`, {
    method: 'POST',
    credentials: 'include',
    headers,
    body: JSON.stringify(payload),
  });

  return readResponse(response);
}

export async function getCurrentSession() {
  const response = await fetch(`${API_URL}/api/auth/session`, {
    credentials: 'include',
  });
  return readResponse(response);
}
