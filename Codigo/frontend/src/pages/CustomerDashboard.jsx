import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import logoRentCar from '../assets/LogoRentCar.png';
import { getMyProfile } from '../services/client';
import { getAutomoveis, getMyOrders } from '../services/orders';

const STATUS_LABELS = {
  PENDENTE: 'Pendente',
  EM_ANALISE: 'Em análise',
  APROVADO: 'Aprovado',
  REJEITADO: 'Rejeitado',
  CANCELADO: 'Cancelado',
};

function formatDate(value) {
  return new Intl.DateTimeFormat('pt-BR', { timeZone: 'UTC' })
    .format(new Date(`${value}T00:00:00Z`));
}

export default function CustomerDashboard() {
  const [profile, setProfile] = useState(null);
  const [orders, setOrders] = useState(null);
  const [vehicleCount, setVehicleCount] = useState(null);
  const [errors, setErrors] = useState([]);
  const [authenticationRequired, setAuthenticationRequired] = useState(false);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    let active = true;

    Promise.allSettled([getMyProfile(), getMyOrders(), getAutomoveis()])
      .then(([profileResult, ordersResult, vehiclesResult]) => {
        if (!active) {
          return;
        }

        const failures = [];
        const recordFailure = (result) => {
          const error = result.reason;
          if (error.status === 401 || error.status === 403) {
            setAuthenticationRequired(true);
          }
          failures.push(error.message === 'Failed to fetch'
            ? 'Não foi possível conectar ao servidor. Verifique se o backend está em execução.'
            : error.message);
        };

        if (profileResult.status === 'fulfilled') {
          setProfile(profileResult.value);
        } else {
          recordFailure(profileResult);
        }

        if (ordersResult.status === 'fulfilled') {
          setOrders(ordersResult.value);
        } else {
          recordFailure(ordersResult);
        }

        if (vehiclesResult.status === 'fulfilled') {
          setVehicleCount(vehiclesResult.value.length);
        } else {
          recordFailure(vehiclesResult);
        }
        setErrors([...new Set(failures)]);
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  const recentOrders = [...(orders || [])]
    .sort((first, second) => new Date(second.dataCriacao) - new Date(first.dataCriacao))
    .slice(0, 3);

  return (
    <main className="auth-page dashboard-page">
      <section className="auth-card auth-card--register dashboard-card" aria-label="Dashboard do cliente">
        <aside className="auth-brand">
          <img className="auth-brand__logo" src={logoRentCar} alt="RentCar - aluguel de veículos" />
        </aside>

        <div className="auth-content">
          <div className="auth-content__inner dashboard-content">
            <span className="auth-eyebrow">ÁREA DO CLIENTE</span>
            <h1 className="auth-title auth-title--register">Olá{profile?.nome ? `, ${profile.nome.split(' ')[0]}` : ''}!</h1>
            <p className="auth-home__description">
              Acompanhe seus pedidos e encontre seu próximo carro.
            </p>

            {errors.map((error, index) => (
              <p className="auth-error" role="alert" key={`${index}-${error}`}>{error}</p>
            ))}
            {authenticationRequired && (
              <p className="auth-error" role="alert">
                Sua sessão expirou. <Link to="/login">Entre novamente</Link> para acessar sua dashboard.
              </p>
            )}

            <div className="dashboard-stats" aria-label="Resumo da conta">
              <article className="dashboard-stat">
                <span className="dashboard-stat__value">{loading || !orders ? '—' : orders.length}</span>
                <span className="dashboard-stat__label">Pedidos realizados</span>
              </article>
              <article className="dashboard-stat">
                <span className="dashboard-stat__value">
                  {loading || !orders ? '—' : orders.filter((order) => order.status === 'EM_ANALISE' || order.status === 'PENDENTE').length}
                </span>
                <span className="dashboard-stat__label">Em análise</span>
              </article>
              <article className="dashboard-stat">
                <span className="dashboard-stat__value">{loading || vehicleCount === null ? '—' : vehicleCount}</span>
                <span className="dashboard-stat__label">Veículos disponíveis</span>
              </article>
            </div>

            <div className="dashboard-actions">
              <Link to="/pedidos" className="auth-submit auth-home__link">Solicitar aluguel</Link>
              <Link to="/cliente" className="auth-submit auth-submit--secondary auth-home__link">Meu perfil</Link>
            </div>

            <section className="dashboard-orders" aria-labelledby="dashboard-orders-title">
              <div className="dashboard-orders__heading">
                <h2 id="dashboard-orders-title" className="orders-list__title">Pedidos recentes</h2>
                <Link to="/pedidos">Ver todos</Link>
              </div>
              {loading && <p className="orders-list__empty">Carregando seus pedidos...</p>}
              {!loading && orders === null && (
                <p className="orders-list__empty">Não foi possível carregar seus pedidos agora.</p>
              )}
              {!loading && orders?.length === 0 && (
                <p className="orders-list__empty">
                  Você ainda não tem pedidos. Quando solicitar um aluguel, ele aparecerá aqui.
                </p>
              )}
              {recentOrders.map((order) => (
                <article className="order-item" key={order.id}>
                  <div className="order-item__heading">
                    <h3>{order.marca} {order.modelo}</h3>
                    <span className={`order-status order-status--${order.status.toLowerCase()}`}>
                      {STATUS_LABELS[order.status] || order.status}
                    </span>
                  </div>
                  <p>{formatDate(order.dataInicio)} – {formatDate(order.dataFim)}</p>
                </article>
              ))}
            </section>

            <p className="auth-footer dashboard-footer"><Link to="/">Voltar ao início</Link></p>
          </div>
        </div>
      </section>
    </main>
  );
}
