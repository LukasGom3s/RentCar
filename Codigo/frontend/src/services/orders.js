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

export async function getAutomoveis() {
  const response = await fetch(`${API_URL}/api/automoveis`, {
    credentials: 'include',
  });
  return readResponse(response);
}

export async function getMyOrders() {
  const response = await fetch(`${API_URL}/api/pedidos/me`, {
    credentials: 'include',
  });
  return readResponse(response);
}

export async function createOrder(order) {
  const response = await fetch(`${API_URL}/api/pedidos/me`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(order),
  });
  return readResponse(response);
}

export async function updateOrder(id, order) {
  const response = await fetch(`${API_URL}/api/pedidos/me/${id}`, {
    method: 'PUT',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(order),
  });
  return readResponse(response);
}

export async function cancelOrder(id) {
  const response = await fetch(`${API_URL}/api/pedidos/me/${id}/cancelamento`, {
    method: 'PATCH',
    credentials: 'include',
  });
  return readResponse(response);
}
