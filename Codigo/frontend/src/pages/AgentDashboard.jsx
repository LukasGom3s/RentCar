import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import logoRentCar from '../assets/LogoRentCar.png';
import { getCurrentSession } from '../services/auth';
import {
  forwardOrderForExecution,
  getAutomoveis,
  getAgentApprovedOrders,
  getAgentOrdersAwaitingExecution,
  getAgentOrdersAwaitingReview,
  submitFinancialOpinion,
  updateAgentOrder,
} from '../services/orders';

const AGENT_TYPE_LABELS = {
  EMPRESA: 'Empresa',
  BANCO: 'Banco',
};

function localDateValue(date = new Date()) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function nextDateValue(value) {
  if (!value) {
    return localDateValue();
  }
  const [year, month, day] = value.split('-').map(Number);
  return localDateValue(new Date(year, month - 1, day + 1));
}

export default function AgentDashboard() {
  const [session, setSession] = useState(null);
  const [pedidos, setPedidos] = useState([]);
  const [pedidosAprovados, setPedidosAprovados] = useState([]);
  const [pedidosEncaminhados, setPedidosEncaminhados] = useState([]);
  const [automoveis, setAutomoveis] = useState([]);
  const [pedidoEditando, setPedidoEditando] = useState(null);
  const [dadosEdicao, setDadosEdicao] = useState(null);
  const [pedidoEmParecer, setPedidoEmParecer] = useState(null);
  const [dadosParecer, setDadosParecer] = useState('');
  const [carregando, setCarregando] = useState(true);
  const [pedidoEmAcao, setPedidoEmAcao] = useState(null);
  const [error, setError] = useState('');
  const [mensagem, setMensagem] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    let active = true;

    async function initialize() {
      try {
        const currentSession = await getCurrentSession();
        if (!active) {
          return;
        }
        if (currentSession.tipoUsuario !== 'AGENTE') {
          navigate('/dashboard', { replace: true });
          return;
        }
        setSession(currentSession);
        const [agentOrders, cars, approvedOrders, executionOrders] = await Promise.all([
          getAgentOrdersAwaitingReview(),
          getAutomoveis(),
          getAgentApprovedOrders(),
          getAgentOrdersAwaitingExecution(),
        ]);
        if (active) {
          setPedidos(agentOrders);
          setAutomoveis(cars);
          setPedidosAprovados(approvedOrders);
          setPedidosEncaminhados(executionOrders);
        }
      } catch (requestError) {
        if (!active) {
          return;
        }
        if (requestError.status === 401 || requestError.status === 403) {
          navigate('/login', {
            replace: true,
            state: { message: 'Entre na sua conta de agente para continuar.' },
          });
          return;
        }
        setError(requestError.message);
      } finally {
        if (active) {
          setCarregando(false);
        }
      }
    }

    initialize();

    return () => {
      active = false;
    };
  }, [navigate]);

  const atualizarFila = async () => {
    setCarregando(true);
    setError('');
    setMensagem('');
    try {
      const [agentOrders, approvedOrders, executionOrders] = await Promise.all([
        getAgentOrdersAwaitingReview(),
        getAgentApprovedOrders(),
        getAgentOrdersAwaitingExecution(),
      ]);
      setPedidos(agentOrders);
      setPedidosAprovados(approvedOrders);
      setPedidosEncaminhados(executionOrders);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setCarregando(false);
    }
  };

  const abrirParecer = (pedido, aprovado) => {
    setError('');
    setMensagem('');
    setPedidoEmParecer({ id: pedido.id, aprovado });
    setDadosParecer('');
  };

  const cancelarParecer = () => {
    setPedidoEmParecer(null);
    setDadosParecer('');
  };

  const avaliarPedido = async (event, pedido) => {
    event.preventDefault();
    if (!pedidoEmParecer || !dadosParecer.trim()) {
      setError('Descreva o parecer antes de registrar a decisão.');
      return;
    }

    const { aprovado } = pedidoEmParecer;
    setPedidoEmAcao(pedido.id);
    setError('');
    setMensagem('');
    try {
      const resultado = await submitFinancialOpinion(pedido.id, aprovado, dadosParecer);
      setPedidos((current) => current.filter((item) => item.id !== pedido.id));
      if (aprovado) {
        setPedidosAprovados((current) => [
          { ...pedido, status: resultado.status, parecerFinanceiro: resultado.parecerFinanceiro },
          ...current.filter((item) => item.id !== pedido.id),
        ]);
      }
      setPedidoEditando(null);
      setDadosEdicao(null);
      cancelarParecer();
      setMensagem(`Parecer ${aprovado ? 'aprovado' : 'reprovado'} registrado para o pedido #${pedido.id}.`);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setPedidoEmAcao(null);
    }
  };

  const encaminharParaExecucao = async (pedido) => {
    setPedidoEmAcao(pedido.id);
    setError('');
    setMensagem('');
    try {
      const result = await forwardOrderForExecution(pedido.id);
      setPedidosAprovados((current) => current.filter((item) => item.id !== pedido.id));
      setPedidosEncaminhados((current) => [
        { ...pedido, status: result.status },
        ...current.filter((item) => item.id !== pedido.id),
      ]);
      setMensagem(`Pedido #${pedido.id} encaminhado para consideração da execução do contrato.`);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setPedidoEmAcao(null);
    }
  };

  const iniciarEdicao = (pedido) => {
    setError('');
    setMensagem('');
    setPedidoEditando(pedido.id);
    setDadosEdicao({
      automovelId: String(pedido.automovelId),
      dataInicio: pedido.dataInicio,
      dataFim: pedido.dataFim,
    });
  };

  const handleChangeEdicao = (event) => {
    setDadosEdicao((current) => ({ ...current, [event.target.name]: event.target.value }));
  };

  const cancelarEdicao = () => {
    setPedidoEditando(null);
    setDadosEdicao(null);
  };

  const salvarEdicao = async (event, pedidoId) => {
    event.preventDefault();
    setPedidoEmAcao(pedidoId);
    setError('');
    setMensagem('');
    try {
      const updatedOrder = await updateAgentOrder(pedidoId, {
        ...dadosEdicao,
        automovelId: Number(dadosEdicao.automovelId),
      });
      setPedidos((current) => current.map((pedido) => (
        pedido.id === pedidoId ? updatedOrder : pedido
      )));
      cancelarEdicao();
      setMensagem(`Pedido #${pedidoId} atualizado.`);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setPedidoEmAcao(null);
    }
  };

  const formatCurrency = (value) => new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(Number(value));

  const formatDate = (value) => new Intl.DateTimeFormat('pt-BR', { timeZone: 'UTC' })
    .format(new Date(`${value}T00:00:00Z`));

  return (
    <main className="auth-page">
      <section className="auth-card auth-card--register agent-dashboard-card" aria-label="Área do agente">
        <aside className="auth-brand">
          <img className="auth-brand__logo" src={logoRentCar} alt="RentCar - aluguel de veículos" />
        </aside>

        <div className="auth-content">
          <div className="auth-content__inner agent-dashboard-content">
            <span className="auth-eyebrow">RENT CAR · ÁREA DO AGENTE</span>
            <h1 className="auth-title auth-title--register">Análise financeira</h1>
            {session && (
              <p className="auth-home__description">
                Bem-vindo, {session.nome || 'Agente'}. Acesso como agente{session.tipoAgente
                  ? ` de ${AGENT_TYPE_LABELS[session.tipoAgente] || session.tipoAgente.toLowerCase()}`
                  : ''}.
              </p>
            )}
            {error && <p className="auth-error" role="alert">{error}</p>}
            {mensagem && <p className="auth-success auth-success--inline" role="status">{mensagem}</p>}

            <section className="orders-list" aria-labelledby="agent-orders-title">
              <div className="orders-list__toolbar">
                <h2 id="agent-orders-title" className="orders-list__title">Pedidos em análise</h2>
                <button
                  className="agent-review__refresh"
                  type="button"
                  onClick={atualizarFila}
                  disabled={carregando || pedidoEmAcao !== null}
                >
                  Atualizar fila
                </button>
              </div>

              {carregando && <p className="orders-list__empty">Carregando pedidos...</p>}
              {!carregando && pedidos.length === 0 && (
                <p className="orders-list__empty">Não há pedidos aguardando análise financeira.</p>
              )}
              {pedidos.map((pedido) => (
                <article className="order-item agent-review-item" key={pedido.id}>
                  <div className="order-item__heading">
                    <h3>Pedido #{pedido.id} · {pedido.clienteNome}</h3>
                    <span className="order-status">Em análise</span>
                  </div>
                  <p>{pedido.clienteProfissao || 'Profissão não informada'}</p>
                  <p>
                    {pedido.marca} {pedido.modelo} ({pedido.ano}) · Placa {pedido.placa}
                  </p>
                  <p>Período: {formatDate(pedido.dataInicio)} – {formatDate(pedido.dataFim)}</p>

                  <section className="agent-review__income" aria-label="Rendimentos do cliente">
                    <h4>Rendimentos declarados</h4>
                    {pedido.rendimentos.length === 0 ? (
                      <p>Nenhuma entidade empregadora ou rendimento informado.</p>
                    ) : (
                      <ul>
                        {pedido.rendimentos.map((rendimento, index) => (
                          <li key={`${rendimento.entidadeEmpregadora}-${index}`}>
                            <span>{rendimento.entidadeEmpregadora}</span>
                            <strong>{formatCurrency(rendimento.valor)}</strong>
                          </li>
                        ))}
                      </ul>
                    )}
                  </section>

                  {pedidoEditando === pedido.id && dadosEdicao ? (
                    <form
                      className="agent-review__edit-form"
                      onSubmit={(event) => salvarEdicao(event, pedido.id)}
                    >
                      <label>
                        Automóvel
                        <select
                          className="auth-input"
                          name="automovelId"
                          value={dadosEdicao.automovelId}
                          onChange={handleChangeEdicao}
                          required
                        >
                          {automoveis.map((automovel) => (
                            <option key={automovel.id} value={automovel.id}>
                              {automovel.marca} {automovel.modelo} ({automovel.ano}) · {automovel.placa}
                            </option>
                          ))}
                        </select>
                      </label>
                      <div className="orders-form__dates">
                        <label>
                          Data inicial
                          <input
                            className="auth-input"
                            type="date"
                            name="dataInicio"
                            value={dadosEdicao.dataInicio}
                            onChange={handleChangeEdicao}
                            min={localDateValue()}
                            required
                          />
                        </label>
                        <label>
                          Data final
                          <input
                            className="auth-input"
                            type="date"
                            name="dataFim"
                            value={dadosEdicao.dataFim}
                            onChange={handleChangeEdicao}
                            min={nextDateValue(dadosEdicao.dataInicio)}
                            required
                          />
                        </label>
                      </div>
                      <div className="order-actions">
                        <button
                          className="auth-submit"
                          type="submit"
                          disabled={pedidoEmAcao !== null}
                        >
                          {pedidoEmAcao === pedido.id ? 'Salvando...' : 'Salvar alterações'}
                        </button>
                        <button
                          className="auth-submit auth-submit--secondary"
                          type="button"
                          onClick={cancelarEdicao}
                          disabled={pedidoEmAcao !== null}
                        >
                          Voltar
                        </button>
                      </div>
                    </form>
                  ) : (
                    <>
                      <div className="order-actions agent-review__edit-action">
                        <button
                          className="auth-submit auth-submit--secondary"
                          type="button"
                          onClick={() => iniciarEdicao(pedido)}
                          disabled={pedidoEmAcao !== null}
                        >
                          Editar automóvel e período
                        </button>
                      </div>
                      {pedidoEmParecer?.id === pedido.id ? (
                        <form
                          className="agent-review__opinion-form"
                          onSubmit={(event) => avaliarPedido(event, pedido)}
                        >
                          <label htmlFor={`parecer-${pedido.id}`}>
                            Parecer financeiro — {pedidoEmParecer.aprovado ? 'aprovação' : 'reprovação'}
                          </label>
                          <textarea
                            id={`parecer-${pedido.id}`}
                            className="auth-input"
                            value={dadosParecer}
                            onChange={(event) => setDadosParecer(event.target.value)}
                            placeholder="Registre a justificativa da decisão com base nos dados financeiros."
                            maxLength={2000}
                            rows={4}
                            required
                          />
                          <small>{dadosParecer.length}/2000 caracteres</small>
                          <div className="order-actions">
                            <button
                              className={`auth-submit${pedidoEmParecer.aprovado ? '' : ' auth-submit--danger'}`}
                              type="submit"
                              disabled={pedidoEmAcao !== null || !dadosParecer.trim()}
                            >
                              {pedidoEmAcao === pedido.id ? 'Registrando...' : 'Registrar parecer'}
                            </button>
                            <button
                              className="auth-submit auth-submit--secondary"
                              type="button"
                              onClick={cancelarParecer}
                              disabled={pedidoEmAcao !== null}
                            >
                              Voltar
                            </button>
                          </div>
                        </form>
                      ) : (
                      <div className="order-actions">
                        <button
                          className="auth-submit"
                          type="button"
                          onClick={() => abrirParecer(pedido, true)}
                          disabled={pedidoEmAcao !== null}
                        >
                          Aprovar parecer
                        </button>
                        <button
                          className="auth-submit auth-submit--danger"
                          type="button"
                          onClick={() => abrirParecer(pedido, false)}
                          disabled={pedidoEmAcao !== null}
                        >
                          Reprovar parecer
                        </button>
                      </div>
                      )}
                    </>
                  )}
                </article>
              ))}
            </section>

            <section className="orders-list" aria-labelledby="approved-orders-title">
              <div className="orders-list__toolbar">
                <h2 id="approved-orders-title" className="orders-list__title">Pareceres positivos</h2>
              </div>
              {!carregando && pedidosAprovados.length === 0 && (
                <p className="orders-list__empty">Não há pedidos aprovados aguardando encaminhamento.</p>
              )}
              {pedidosAprovados.map((pedido) => (
                <article className="order-item agent-review-item" key={pedido.id}>
                  <div className="order-item__heading">
                    <h3>Pedido #{pedido.id} · {pedido.clienteNome}</h3>
                    <span className="order-status order-status--aprovado">Aprovado</span>
                  </div>
                  <p>{pedido.marca} {pedido.modelo} ({pedido.ano}) · Placa {pedido.placa}</p>
                  <p>Período: {formatDate(pedido.dataInicio)} – {formatDate(pedido.dataFim)}</p>
                  {pedido.parecerFinanceiro && (
                    <p className="agent-review__saved-opinion">
                      <strong>Parecer:</strong> {pedido.parecerFinanceiro}
                    </p>
                  )}
                  <button
                    className="auth-submit agent-review__forward"
                    type="button"
                    onClick={() => encaminharParaExecucao(pedido)}
                    disabled={pedidoEmAcao !== null}
                  >
                    {pedidoEmAcao === pedido.id
                      ? 'Encaminhando...'
                      : 'Colocar para consideração da execução'}
                  </button>
                </article>
              ))}
            </section>

            <section className="orders-list" aria-labelledby="execution-orders-title">
              <div className="orders-list__toolbar">
                <h2 id="execution-orders-title" className="orders-list__title">
                  Encaminhados para execução do contrato
                </h2>
              </div>
              {!carregando && pedidosEncaminhados.length === 0 && (
                <p className="orders-list__empty">Nenhum pedido foi encaminhado para execução.</p>
              )}
              {pedidosEncaminhados.map((pedido) => (
                <article className="order-item agent-review-item" key={pedido.id}>
                  <div className="order-item__heading">
                    <h3>Pedido #{pedido.id} · {pedido.clienteNome}</h3>
                    <span className="order-status order-status--aguardando_execucao">
                      Aguardando execução do contrato
                    </span>
                  </div>
                  <p>{pedido.marca} {pedido.modelo} ({pedido.ano}) · Placa {pedido.placa}</p>
                  <p>Período: {formatDate(pedido.dataInicio)} – {formatDate(pedido.dataFim)}</p>
                  {pedido.parecerFinanceiro && (
                    <p className="agent-review__saved-opinion">
                      <strong>Parecer:</strong> {pedido.parecerFinanceiro}
                    </p>
                  )}
                </article>
              ))}
            </section>
            <p className="auth-footer"><Link to="/">Voltar ao início</Link></p>
          </div>
        </div>
      </section>
    </main>
  );
}
