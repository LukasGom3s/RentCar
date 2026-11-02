import API_URL from './api';

async function readResponse(response) {
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

export async function createAgent(agent) {
  const response = await fetch(`${API_URL}/api/admin/agentes`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(agent),
  });
  return readResponse(response);
}

export async function listAgents() {
  const response = await fetch(`${API_URL}/api/admin/agentes`, {
    credentials: 'include',
  });
  return readResponse(response);
}

export async function getAgent(id) {
  const response = await fetch(`${API_URL}/api/admin/agentes/${id}`, {
    credentials: 'include',
  });
  return readResponse(response);
}

export async function updateAgent(id, agent) {
  const response = await fetch(`${API_URL}/api/admin/agentes/${id}`, {
    method: 'PUT',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(agent),
  });
  return readResponse(response);
}
