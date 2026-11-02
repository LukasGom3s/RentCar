import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import logoRentCar from '../assets/LogoRentCar.png';
import { getCurrentSession } from '../services/auth';
import {
  getAgentOrdersAwaitingReview,
  submitFinancialOpinion,
} from '../services/orders';

const AGENT_TYPE_LABELS = {
  EMPRESA: 'Empresa',
  BANCO: 'Banco',
};

export default function AgentDashboard() {
  const [session, setSession] = useState(null);
  const [pedidos, setPedidos] = useState([]);
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
        setPedidos(await getAgentOrdersAwaitingReview());
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
      setPedidos(await getAgentOrdersAwaitingReview());
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setCarregando(false);
    }
  };

  const avaliarPedido = async (pedido, aprovado) => {
    setPedidoEmAcao(pedido.id);
    setError('');
    setMensagem('');
    try {
      await submitFinancialOpinion(pedido.id, aprovado);
      setPedidos((current) => current.filter((item) => item.id !== pedido.id));
      setMensagem(`Parecer ${aprovado ? 'aprovado' : 'reprovado'} registrado para o pedido #${pedido.id}.`);
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

                  <div className="order-actions">
                    <button
                      className="auth-submit"
                      type="button"
                      onClick={() => avaliarPedido(pedido, true)}
                      disabled={pedidoEmAcao !== null}
                    >
                      {pedidoEmAcao === pedido.id ? 'Registrando...' : 'Aprovar parecer'}
                    </button>
                    <button
                      className="auth-submit auth-submit--danger"
                      type="button"
                      onClick={() => avaliarPedido(pedido, false)}
                      disabled={pedidoEmAcao !== null}
                    >
                      {pedidoEmAcao === pedido.id ? 'Registrando...' : 'Reprovar parecer'}
                    </button>
                  </div>
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
