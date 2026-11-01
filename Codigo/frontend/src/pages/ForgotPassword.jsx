import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import logoRentCar from '../assets/LogoRentCar.png';
import { submitAuthRequest } from '../services/auth';

export default function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [erro, setErro] = useState('');
  const [mensagem, setMensagem] = useState('');
  const [enviando, setEnviando] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setErro('');
    setMensagem('');
    setEnviando(true);

    try {
      const data = await submitAuthRequest('recoverpassword', { email });
      setMensagem(data.mensagem || 'Se o e-mail estiver cadastrado, você receberá as instruções de recuperação.');
    } catch (error) {
      setErro(error.message);
    } finally {
      setEnviando(false);
    }
  };

  return (
    <main className="auth-page">
      <section className="auth-card" aria-label="Recuperação de senha">
        <aside className="auth-brand">
          <img className="auth-brand__logo" src={logoRentCar} alt="RentCar - aluguel de veículos" />
        </aside>

        <div className="auth-content">
          <div className="auth-content__inner">
            <span className="auth-eyebrow">RENT CAR</span>
            <h1 className="auth-title">Esqueceu sua senha?</h1>
            <p className="auth-home__description">
              Informe seu e-mail e enviaremos um link para redefinir sua senha.
            </p>

            <form className="auth-form" onSubmit={handleSubmit}>
              <label className="visually-hidden" htmlFor="recovery-email">E-mail</label>
              <input
                className="auth-input"
                id="recovery-email"
                type="email"
                name="email"
                placeholder="E-mail"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                autoComplete="email"
                required
              />
              {erro && <p className="auth-error" role="alert">{erro}</p>}
              {mensagem && <p className="auth-success auth-success--inline" role="status">{mensagem}</p>}
              <button className="auth-submit" type="submit" disabled={enviando}>
                {enviando ? 'Enviando...' : 'Enviar link de recuperação'}
              </button>
            </form>

            <p className="auth-footer">
              <Link to="/login">Voltar para o login</Link>
            </p>
          </div>
        </div>
      </section>
    </main>
  );
}
