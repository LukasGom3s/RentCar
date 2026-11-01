import React, { useCallback, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import logoRentCar from '../assets/LogoRentCar.png';
import RecaptchaWidget from '../components/RecaptchaWidget';
import { submitAuthRequest } from '../services/auth';

export default function Register() {
  const [formData, setFormData] = useState({
    nome: '',
    email: '',
    cpf: '',
    rg: '',
    morada: '',
    profissao: '',
    senha: ''
  });
  const [erro, setErro] = useState('');
  const [captchaToken, setCaptchaToken] = useState('');
  const [captchaVersion, setCaptchaVersion] = useState(0);
  const [enviando, setEnviando] = useState(false);
  const navigate = useNavigate();

  const handleCaptchaChange = useCallback((token) => {
    setCaptchaToken(token);
  }, []);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setErro('');

    if (!captchaToken) {
      setErro('Confirme o reCAPTCHA para continuar.');
      return;
    }

    setEnviando(true);
    try {
      const data = await submitAuthRequest('register', formData, captchaToken);
      navigate('/login', {
        replace: true,
        state: { message: data.mensagem || 'Cadastro realizado. Faça login para continuar.' },
      });
    } catch (err) {
      setErro(err.message);
      setCaptchaToken('');
      setCaptchaVersion((version) => version + 1);
    } finally {
      setEnviando(false);
    }
  };

  return (
    <main className="auth-page">
      <section className="auth-card auth-card--register" aria-label="Cadastro RentCar">
        <aside className="auth-brand">
          <img className="auth-brand__logo" src={logoRentCar} alt="RentCar - aluguel de veículos" />
        </aside>

        <div className="auth-content">
          <div className="auth-content__inner">
            <span className="auth-eyebrow">RENT CAR</span>
            <h1 className="auth-title auth-title--register">Crie sua conta</h1>

            {erro && <p className="auth-error" role="alert">{erro}</p>}

            <form onSubmit={handleRegister} className="auth-form">
              <input
                type="text"
                name="nome"
                placeholder="Nome completo"
                aria-label="Nome completo"
                value={formData.nome}
                onChange={handleChange}
                required
                autoComplete="name"
                className="auth-input"
              />
              <input
                type="text"
                name="cpf"
                placeholder="CPF"
                aria-label="CPF"
                value={formData.cpf}
                onChange={handleChange}
                required
                className="auth-input"
              />
              <input
                type="text"
                name="rg"
                placeholder="RG"
                aria-label="RG"
                value={formData.rg}
                onChange={handleChange}
                required
                className="auth-input"
              />
              <input
                type="text"
                name="morada"
                placeholder="Morada"
                aria-label="Morada"
                value={formData.morada}
                onChange={handleChange}
                required
                autoComplete="street-address"
                className="auth-input"
              />
              <input
                type="text"
                name="profissao"
                placeholder="Profissão"
                aria-label="Profissão"
                value={formData.profissao}
                onChange={handleChange}
                required
                className="auth-input"
              />
              <input
                type="email"
                name="email"
                placeholder="E-mail"
                aria-label="E-mail"
                value={formData.email}
                onChange={handleChange}
                required
                autoComplete="email"
                className="auth-input"
              />
              <input
                type="password"
                name="senha"
                placeholder="Senha"
                aria-label="Senha"
                value={formData.senha}
                onChange={handleChange}
                required
                autoComplete="new-password"
                className="auth-input"
              />

              <RecaptchaWidget key={captchaVersion} onChange={handleCaptchaChange} />
              <button type="submit" className="auth-submit" disabled={enviando}>
                {enviando ? 'Cadastrando...' : 'Registrar'}
              </button>
            </form>

            <p className="auth-footer">
              Já possui conta? <Link to="/login">Faça login</Link>
            </p>
          </div>
        </div>
      </section>
    </main>
  );
}