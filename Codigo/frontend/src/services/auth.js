const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

async function readResponse(response) {
  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    throw new Error(data.erro || 'Não foi possível concluir a operação.');
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
  const response = await fetch(`${API_URL}/api/auth/${endpoint}`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      'X-Recaptcha-Response': captchaToken,
    },
    body: JSON.stringify(payload),
  });

  return readResponse(response);
}
