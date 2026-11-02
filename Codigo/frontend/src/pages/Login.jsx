import React, { useCallback, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import logoRentCar from '../assets/LogoRentCar.png';
import RecaptchaWidget from '../components/RecaptchaWidget';
import { submitAuthRequest } from '../services/auth';

export default function Login() {
  const [formData, setFormData] = useState({ email: '', senha: '' });
  const [captchaToken, setCaptchaToken] = useState('');
  const [captchaRequired, setCaptchaRequired] = useState(true);
  const [captchaVersion, setCaptchaVersion] = useState(0);
  const [erro, setErro] = useState('');
  const [enviando, setEnviando] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const handleCaptchaChange = useCallback((token) => {
    setCaptchaToken(token);
  }, []);
  const handleCaptchaRequiredChange = useCallback((required) => {
    setCaptchaRequired(required);
  }, []);

  const handleChange = (event) => {
    setFormData({ ...formData, [event.target.name]: event.target.value });
  };

  const handleLogin = async (event) => {
    event.preventDefault();
    setErro('');

    if (captchaRequired && !captchaToken) {
      setErro('Confirme o reCAPTCHA para continuar.');
      return;
    }

    setEnviando(true);
    try {
      const session = await submitAuthRequest('login', formData, captchaToken);
      if (!['ADMIN', 'AGENTE', 'CLIENTE'].includes(session.tipoUsuario)) {
        throw new Error('Não foi possível identificar o tipo da conta. Entre em contato com o suporte.');
      }
      const destination = {
        ADMIN: '/admin',
        AGENTE: '/agente',
        CLIENTE: '/dashboard',
      }[session.tipoUsuario];
      navigate(destination, { replace: true });
    } catch (error) {
      setErro(error.message);
      setCaptchaToken('');
      setCaptchaVersion((version) => version + 1);
    } finally {
      setEnviando(false);
    }
  };

  return (
    <main className="auth-page">
      <section className="auth-card" aria-label="Acesso à conta RentCar">
        <aside className="auth-brand">
          <img className="auth-brand__logo" src={logoRentCar} alt="RentCar - aluguel de veículos" />
        </aside>

        <div className="auth-content">
          <div className="auth-content__inner">
            <span className="auth-eyebrow">RENT CAR</span>
            <h1 className="auth-title">Login</h1>
            {location.state?.message && (
              <p className="auth-success" role="status">{location.state.message}</p>
            )}

            <form className="auth-form" onSubmit={handleLogin}>
              <label className="visually-hidden" htmlFor="login-email">E-mail</label>
              <input
                className="auth-input"
                id="login-email"
                type="email"
                name="email"
                placeholder="E-mail"
                value={formData.email}
                onChange={handleChange}
                autoComplete="email"
                required
              />
              <label className="visually-hidden" htmlFor="login-password">Senha</label>
              <input
                className="auth-input"
                id="login-password"
                type="password"
                name="senha"
                placeholder="Senha"
                value={formData.senha}
                onChange={handleChange}
                autoComplete="current-password"
                required
              />
              {erro && <p className="auth-error" role="alert">{erro}</p>}
              <RecaptchaWidget
                key={captchaVersion}
                onChange={handleCaptchaChange}
                onRequiredChange={handleCaptchaRequiredChange}
              />
              <button className="auth-submit" type="submit" disabled={enviando}>
                {enviando ? 'Entrando...' : 'Entrar'}
              </button>
            </form>

            <p className="auth-footer">
              <span>Ainda não tem cadastro?</span>
              <span className="auth-footer__links">
                <Link to="/register">Cadastre-se</Link>
                <span className="auth-footer__separator" aria-hidden="true">|</span>
                <Link to="/forgot-password">Esqueceu sua senha?</Link>
              </span>
            </p>
          </div>
        </div>
      </section>
    </main>
  );
}