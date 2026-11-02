import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import logoRentCar from '../assets/LogoRentCar.png';
import { getCurrentSession } from '../services/auth';

const AGENT_TYPE_LABELS = {
  EMPRESA: 'Empresa',
  BANCO: 'Banco',
};

export default function AgentDashboard() {
  const [session, setSession] = useState(null);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    let active = true;

    getCurrentSession()
      .then((currentSession) => {
        if (!active) {
          return;
        }
        if (currentSession.tipoUsuario !== 'AGENTE') {
          navigate('/dashboard', { replace: true });
          return;
        }
        setSession(currentSession);
      })
      .catch((requestError) => {
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
      });

    return () => {
      active = false;
    };
  }, [navigate]);

  return (
    <main className="auth-page">
      <section className="auth-card auth-card--register" aria-label="Área do agente">
        <aside className="auth-brand">
          <img className="auth-brand__logo" src={logoRentCar} alt="RentCar - aluguel de veículos" />
        </aside>

        <div className="auth-content">
          <div className="auth-content__inner">
            <span className="auth-eyebrow">RENT CAR · ÁREA DO AGENTE</span>
            <h1 className="auth-title auth-title--register">Bem-vindo{session ? `, ${session.email}` : ''}</h1>
            {session && (
              <p className="auth-home__description">
                Acesso autenticado como agente{session.tipoAgente
                  ? ` de ${AGENT_TYPE_LABELS[session.tipoAgente] || session.tipoAgente.toLowerCase()}`
                  : ''}.
              </p>
            )}
            {!session && !error && (
              <p className="auth-home__description">Verificando seu acesso...</p>
            )}
            {error && <p className="auth-error" role="alert">{error}</p>}
            {session && (
              <p className="auth-home__description">
                Seu acesso foi reconhecido. As ferramentas de análise de pedidos e contratos serão
                disponibilizadas nas próximas histórias do sistema.
              </p>
            )}
            <p className="auth-footer"><Link to="/">Voltar ao início</Link></p>
          </div>
        </div>
      </section>
    </main>
  );
}
