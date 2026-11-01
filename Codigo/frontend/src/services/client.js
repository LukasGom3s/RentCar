import API_URL from './api';

async function readResponse(response) {
  if (response.status === 204) {
    return null;
  }

  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    const error = new Error(
      data.message || data.erro || data.detail || 'Não foi possível concluir a operação.',
    );
    error.status = response.status;
    throw error;
  }

  return data;
}

export async function getMyProfile() {
  const response = await fetch(`${API_URL}/api/clientes/me`, {
    credentials: 'include',
  });
  return readResponse(response);
}

export async function updateMyProfile(profile) {
  const response = await fetch(`${API_URL}/api/clientes/me`, {
    method: 'PUT',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(profile),
  });
  return readResponse(response);
}

export async function deleteMyProfile() {
  const response = await fetch(`${API_URL}/api/clientes/me`, {
    method: 'DELETE',
    credentials: 'include',
  });
  return readResponse(response);
}
