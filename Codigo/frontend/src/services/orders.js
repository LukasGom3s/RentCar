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

export async function getAgentOrdersAwaitingReview() {
  const response = await fetch(`${API_URL}/api/agente/pedidos`, {
    credentials: 'include',
  });
  return readResponse(response);
}

export async function getAgentApprovedOrders() {
  const response = await fetch(`${API_URL}/api/agente/pedidos/aprovados`, {
    credentials: 'include',
  });
  return readResponse(response);
}

export async function getAgentOrdersAwaitingExecution() {
  const response = await fetch(`${API_URL}/api/agente/pedidos/execucao`, {
    credentials: 'include',
  });
  return readResponse(response);
}

export async function getAgentContracts() {
  const response = await fetch(`${API_URL}/api/agente/contratos`, {
    credentials: 'include',
  });
  return readResponse(response);
}

export async function executeContractForOrder(pedidoId, detalhesCreditoBanco) {
  const response = await fetch(`${API_URL}/api/agente/contratos/pedidos/${pedidoId}/execucao`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ detalhesCreditoBanco }),
  });
  return readResponse(response);
}

export async function getMyContracts() {
  const response = await fetch(`${API_URL}/api/contratos/me`, {
    credentials: 'include',
  });
  return readResponse(response);
}

export async function submitFinancialOpinion(id, aprovado, parecer) {
  const response = await fetch(`${API_URL}/api/agente/pedidos/${id}/parecer`, {
    method: 'PATCH',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ aprovado, parecer }),
  });
  return readResponse(response);
}

export async function forwardOrderForExecution(id) {
  const response = await fetch(`${API_URL}/api/agente/pedidos/${id}/execucao`, {
    method: 'PATCH',
    credentials: 'include',
  });
  return readResponse(response);
}

export async function updateAgentOrder(id, order) {
  const response = await fetch(`${API_URL}/api/agente/pedidos/${id}`, {
    method: 'PUT',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(order),
  });
  return readResponse(response);
}
