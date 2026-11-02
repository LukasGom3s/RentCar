import React, { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import logoRentCar from '../assets/LogoRentCar.png';
import {
  cancelOrder,
  getAutomoveis,
  getMyOrders,
  updateOrder,
} from '../services/orders';

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

function formatDate(value) {
  return new Intl.DateTimeFormat('pt-BR', { timeZone: 'UTC' }).format(new Date(`${value}T00:00:00Z`));
}

const STATUS_LABELS = {
  PENDENTE: 'Pendente',
  EM_ANALISE: 'Em análise',
  APROVADO: 'Aprovado',
  AGUARDANDO_EXECUCAO: 'Aguardando execução do contrato',
  REJEITADO: 'Rejeitado',
  CANCELADO: 'Cancelado',
};

export default function Orders() {
  const [automoveis, setAutomoveis] = useState([]);
  const [pedidos, setPedidos] = useState([]);
  const [filtroStatus, setFiltroStatus] = useState('TODOS');
  const [pedidoEditando, setPedidoEditando] = useState(null);
  const [dadosEdicao, setDadosEdicao] = useState(null);
  const [pedidoEmAcao, setPedidoEmAcao] = useState(null);
  const [carregando, setCarregando] = useState(true);
  const [sessaoAtiva, setSessaoAtiva] = useState(null);
  const [erro, setErro] = useState('');
  const [mensagem, setMensagem] = useState('');
  const location = useLocation();

  useEffect(() => {
    let active = true;

    Promise.allSettled([getAutomoveis(), getMyOrders()])
      .then(([carsResult, ordersResult]) => {
        if (!active) {
          return;
        }

        if (carsResult.status === 'fulfilled') {
          setAutomoveis(carsResult.value);
        } else {
          setErro(carsResult.reason.message);
        }

        if (ordersResult.status === 'fulfilled') {
          setPedidos(ordersResult.value);
          setSessaoAtiva(true);
        } else if (ordersResult.reason.status === 401 || ordersResult.reason.status === 403) {
          setSessaoAtiva(false);
        } else if (carsResult.status === 'fulfilled') {
          setErro(ordersResult.reason.message);
        }
      })
      .finally(() => {
        if (active) {
          setCarregando(false);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  const iniciarEdicao = (pedido) => {
    setErro('');
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
    setErro('');
    setMensagem('');
    setPedidoEmAcao(pedidoId);

    try {
      const updatedOrder = await updateOrder(pedidoId, {
        ...dadosEdicao,
        automovelId: Number(dadosEdicao.automovelId),
      });
      setPedidos((current) => current.map((pedido) => (
        pedido.id === pedidoId ? updatedOrder : pedido
      )));
      cancelarEdicao();
      setMensagem('Pedido atualizado e enviado para análise.');
    } catch (error) {
      setErro(error.message);
    } finally {
      setPedidoEmAcao(null);
    }
  };

  const handleCancelOrder = async (pedido) => {
    if (!window.confirm('Deseja cancelar este pedido de aluguel?')) {
      return;
    }

    setErro('');
    setMensagem('');
    setPedidoEmAcao(pedido.id);

    try {
      const cancelledOrder = await cancelOrder(pedido.id);
      setPedidos((current) => current.map((item) => (
        item.id === pedido.id ? cancelledOrder : item
      )));
      if (pedidoEditando === pedido.id) {
        cancelarEdicao();
      }
      setMensagem('Pedido cancelado.');
    } catch (error) {
      setErro(error.message);
    } finally {
      setPedidoEmAcao(null);
    }
  };

  const pedidosFiltrados = filtroStatus === 'TODOS'
    ? pedidos
    : pedidos.filter((pedido) => pedido.status === filtroStatus);

  return (
    <main className="auth-page">
      <section className="auth-card auth-card--register orders-card" aria-label="Meus pedidos">
        <aside className="auth-brand">
          <img className="auth-brand__logo" src={logoRentCar} alt="RentCar - aluguel de veículos" />
        </aside>

        <div className="auth-content">
          <div className="auth-content__inner">
            <span className="auth-eyebrow">RENT CAR</span>
            <h1 className="auth-title auth-title--register">Meus pedidos</h1>
            <p className="auth-home__description">
              Acompanhe seus pedidos de aluguel e filtre pelo status.
            </p>

            {erro && <p className="auth-error" role="alert">{erro}</p>}
            {location.state?.message && (
              <p className="auth-success auth-success--inline" role="status">{location.state.message}</p>
            )}
            {mensagem && <p className="auth-success auth-success--inline" role="status">{mensagem}</p>}
            {!carregando && sessaoAtiva === false && (
              <p className="auth-error" role="alert">
                Para consultar seus pedidos, <Link to="/login">entre na sua conta</Link>.
              </p>
            )}

            <section className="orders-list" aria-labelledby="orders-list-title">
              <div className="orders-list__toolbar">
                <h2 id="orders-list-title" className="orders-list__title">Lista de pedidos</h2>
                <label className="orders-filter">
                  Filtrar por status
                  <select
                    className="auth-input"
                    value={filtroStatus}
                    onChange={(event) => setFiltroStatus(event.target.value)}
                    disabled={carregando || pedidos.length === 0}
                  >
                    <option value="TODOS">Todos os status</option>
                    {Object.entries(STATUS_LABELS).map(([status, label]) => (
                      <option key={status} value={status}>{label}</option>
                    ))}
                  </select>
                </label>
              </div>
              {carregando && <p className="orders-list__empty">Carregando pedidos...</p>}
              {!carregando && pedidos.length === 0 && (
                <p className="orders-list__empty">Você ainda não possui pedidos de aluguel.</p>
              )}
              {!carregando && pedidos.length > 0 && pedidosFiltrados.length === 0 && (
                <p className="orders-list__empty">Não há pedidos com esse status.</p>
              )}
              {pedidosFiltrados.map((pedido) => (
                <article className="order-item" key={pedido.id}>
                  <div className="order-item__heading">
                    <h3>{pedido.marca} {pedido.modelo}</h3>
                    <span className={`order-status order-status--${pedido.status.toLowerCase()}`}>
                      {STATUS_LABELS[pedido.status] || pedido.status}
                    </span>
                  </div>
                  <p>{pedido.ano} · Placa {pedido.placa}</p>
                  <p>{formatDate(pedido.dataInicio)} – {formatDate(pedido.dataFim)}</p>
                  {pedido.parecerFinanceiro && (
                    <p className="order-financial-opinion">
                      <strong>Parecer do agente:</strong> {pedido.parecerFinanceiro}
                    </p>
                  )}
                  <small>Solicitado em {new Intl.DateTimeFormat('pt-BR').format(new Date(pedido.dataCriacao))}</small>
                  {pedidoEditando === pedido.id && dadosEdicao ? (
                    <form
                      className="order-edit-form"
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
                        <button className="auth-submit" type="submit" disabled={pedidoEmAcao === pedido.id}>
                          {pedidoEmAcao === pedido.id ? 'Salvando...' : 'Salvar alterações'}
                        </button>
                        <button
                          className="auth-submit auth-submit--secondary"
                          type="button"
                          onClick={cancelarEdicao}
                          disabled={pedidoEmAcao === pedido.id}
                        >
                          Voltar
                        </button>
                      </div>
                    </form>
                  ) : (
                    (pedido.status === 'PENDENTE'
                      || pedido.status === 'EM_ANALISE'
                      || pedido.status === 'APROVADO') && (
                        <div className="order-actions">
                          <button
                            className="auth-submit auth-submit--secondary"
                            type="button"
                            onClick={() => iniciarEdicao(pedido)}
                            disabled={pedidoEmAcao !== null}
                          >
                            Editar pedido
                          </button>
                          {(pedido.status === 'PENDENTE' || pedido.status === 'EM_ANALISE') && (
                            <button
                              className="auth-submit auth-submit--danger"
                              type="button"
                              onClick={() => handleCancelOrder(pedido)}
                              disabled={pedidoEmAcao !== null}
                            >
                              {pedidoEmAcao === pedido.id ? 'Cancelando...' : 'Cancelar pedido'}
                            </button>
                          )}
                        </div>
                      )
                  )}
                </article>
              ))}
            </section>

            <p className="auth-footer"><Link to="/dashboard">Voltar à dashboard</Link></p>
          </div>
        </div>
      </section>
    </main>
  );
}
