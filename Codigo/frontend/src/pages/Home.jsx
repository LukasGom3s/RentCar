import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import logoRentCar from '../assets/LogoRentCar.png';

export default function HomeWelcome() {
  const location = useLocation();

  return (
    <main className="auth-page">
      <section className="auth-card auth-card--home" aria-label="Bem-vindo ao RentCar">
        <aside className="auth-brand">
          <img className="auth-brand__logo" src={logoRentCar} alt="RentCar - aluguel de veículos" />
        </aside>

        <div className="auth-content">
          <div className="auth-content__inner">
            <span className="auth-eyebrow">Área do cliente</span>
            <h1 className="auth-title auth-home__title">Bem-vindo!</h1>
            {location.state?.message && (
              <p className="auth-success" role="status">{location.state.message}</p>
            )}
            <p className="auth-home__description">
              Acesse sua conta ou cadastre-se para começar a alugar seu próximo veículo.
            </p>

            <div className="auth-home__actions">
              <Link to="/login" className="auth-submit auth-home__link">
                Fazer login
              </Link>
              <Link to="/register" className="auth-submit auth-submit--secondary auth-home__link">
                Criar uma conta
              </Link>
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}
