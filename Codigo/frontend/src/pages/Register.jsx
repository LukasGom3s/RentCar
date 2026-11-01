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
    entidadesEmpregadoras: [],
    senha: '',
    confirmarSenha: ''
  });
  const [erro, setErro] = useState('');
  const [errosCampos, setErrosCampos] = useState({});
  const [captchaToken, setCaptchaToken] = useState('');
  const [captchaVersion, setCaptchaVersion] = useState(0);
  const [enviando, setEnviando] = useState(false);
  const [mostrarSenha, setMostrarSenha] = useState(false);
  const [mostrarConfirmacao, setMostrarConfirmacao] = useState(false);
  const navigate = useNavigate();

  const handleCaptchaChange = useCallback((token) => {
    setCaptchaToken(token);
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    const sanitizedValue = name === 'cpf'
      ? value.replace(/\D/g, '')
      : name === 'rg'
        ? value.replace(/[^a-zA-Z0-9]/g, '')
        : value;

    setFormData({ ...formData, [name]: sanitizedValue });
    setErrosCampos((errosAtuais) => {
      if (!errosAtuais[name] && !(name === 'confirmarSenha' && errosAtuais.senha)) {
        return errosAtuais;
      }

      const novosErros = { ...errosAtuais };
      delete novosErros[name];
      if (name === 'senha' || name === 'confirmarSenha') {
        delete novosErros.senha;
        delete novosErros.confirmarSenha;
      }
      return novosErros;
    });
  };

  const handleEmployerChange = (index, field, value) => {
    setFormData((current) => ({
      ...current,
      entidadesEmpregadoras: current.entidadesEmpregadoras.map((entidade, itemIndex) => (
        itemIndex === index ? { ...entidade, [field]: value } : entidade
      )),
    }));
  };

  const addEmployer = () => {
    setFormData((current) => ({
      ...current,
      entidadesEmpregadoras: [
        ...current.entidadesEmpregadoras,
        { nome: '', rendimentoAuferido: '' },
      ],
    }));
  };

  const removeEmployer = (index) => {
    setFormData((current) => ({
      ...current,
      entidadesEmpregadoras: current.entidadesEmpregadoras.filter((_, itemIndex) => itemIndex !== index),
    }));
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setErro('');

    const novosErros = {};
    const camposObrigatorios = ['nome', 'cpf', 'rg', 'morada', 'profissao', 'email', 'senha', 'confirmarSenha'];

    camposObrigatorios.forEach((campo) => {
      if (!formData[campo].trim()) {
        novosErros[campo] = 'Este campo é obrigatório.';
      }
    });

    if (formData.cpf && !/^\d+$/.test(formData.cpf)) {
      novosErros.cpf = 'O CPF deve conter somente números.';
    }

    if (formData.rg && !/^[a-zA-Z0-9]+$/.test(formData.rg)) {
      novosErros.rg = 'O RG deve conter somente letras e números.';
    }

    if (formData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      novosErros.email = 'Informe um e-mail válido.';
    }

    if (formData.senha && formData.confirmarSenha && formData.senha !== formData.confirmarSenha) {
      novosErros.confirmarSenha = 'As senhas não coincidem.';
    }

    formData.entidadesEmpregadoras.forEach((entidade, index) => {
      if (!entidade.nome.trim()) {
        novosErros[`entidade-${index}`] = 'Informe o nome da entidade empregadora.';
      }
      if (!entidade.rendimentoAuferido || Number(entidade.rendimentoAuferido) <= 0) {
        novosErros[`rendimento-${index}`] = 'Informe um rendimento maior que zero.';
      }
    });

    setErrosCampos(novosErros);
    if (Object.keys(novosErros).length > 0) {
      return;
    }

    if (!captchaToken) {
      setErro('Confirme o reCAPTCHA para continuar.');
      return;
    }

    setEnviando(true);
    try {
      const payload = {
        ...formData,
        entidadesEmpregadoras: formData.entidadesEmpregadoras.map((entidade) => ({
          ...entidade,
          rendimentoAuferido: Number(entidade.rendimentoAuferido),
        })),
      };
      const data = await submitAuthRequest('register', payload, captchaToken);
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

            <form onSubmit={handleRegister} className="auth-form" noValidate>
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
                aria-invalid={Boolean(errosCampos.nome)}
                aria-describedby={errosCampos.nome ? 'nome-error' : undefined}
              />
              {errosCampos.nome && <small className="auth-field-error" id="nome-error">{errosCampos.nome}</small>}
              <input
                type="text"
                name="cpf"
                placeholder="CPF"
                aria-label="CPF"
                value={formData.cpf}
                onChange={handleChange}
                required
                inputMode="numeric"
                autoComplete="off"
                className="auth-input"
                aria-invalid={Boolean(errosCampos.cpf)}
                aria-describedby={errosCampos.cpf ? 'cpf-error' : undefined}
              />
              {errosCampos.cpf && <small className="auth-field-error" id="cpf-error">{errosCampos.cpf}</small>}
              <input
                type="text"
                name="rg"
                placeholder="RG"
                aria-label="RG"
                value={formData.rg}
                onChange={handleChange}
                required
                autoComplete="off"
                className="auth-input"
                aria-invalid={Boolean(errosCampos.rg)}
                aria-describedby={errosCampos.rg ? 'rg-error' : undefined}
              />
              {errosCampos.rg && <small className="auth-field-error" id="rg-error">{errosCampos.rg}</small>}
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
                aria-invalid={Boolean(errosCampos.morada)}
                aria-describedby={errosCampos.morada ? 'morada-error' : undefined}
              />
              {errosCampos.morada && <small className="auth-field-error" id="morada-error">{errosCampos.morada}</small>}
              <input
                type="text"
                name="profissao"
                placeholder="Profissão"
                aria-label="Profissão"
                value={formData.profissao}
                onChange={handleChange}
                required
                className="auth-input"
                aria-invalid={Boolean(errosCampos.profissao)}
                aria-describedby={errosCampos.profissao ? 'profissao-error' : undefined}
              />
              {errosCampos.profissao && <small className="auth-field-error" id="profissao-error">{errosCampos.profissao}</small>}
              <section className="employers-form" aria-labelledby="employers-title">
                <div className="employers-form__heading">
                  <div>
                    <h2 id="employers-title">Entidades empregadoras</h2>
                    <p>Opcional. Adicione até três fontes de rendimento.</p>
                  </div>
                  {formData.entidadesEmpregadoras.length < 3 && (
                    <button className="employer-add" type="button" onClick={addEmployer}>
                      Adicionar
                    </button>
                  )}
                </div>
                {formData.entidadesEmpregadoras.map((entidade, index) => (
                  <fieldset className="employer-fields" key={index}>
                    <legend>Entidade {index + 1}</legend>
                    <input
                      className="auth-input"
                      type="text"
                      value={entidade.nome}
                      onChange={(event) => handleEmployerChange(index, 'nome', event.target.value)}
                      placeholder="Nome da entidade empregadora"
                      aria-label={`Nome da entidade empregadora ${index + 1}`}
                      required
                      aria-invalid={Boolean(errosCampos[`entidade-${index}`])}
                    />
                    {errosCampos[`entidade-${index}`] && (
                      <small className="auth-field-error">{errosCampos[`entidade-${index}`]}</small>
                    )}
                    <input
                      className="auth-input"
                      type="number"
                      min="0.01"
                      step="0.01"
                      value={entidade.rendimentoAuferido}
                      onChange={(event) => handleEmployerChange(index, 'rendimentoAuferido', event.target.value)}
                      placeholder="Rendimento mensal"
                      aria-label={`Rendimento da entidade empregadora ${index + 1}`}
                      required
                      aria-invalid={Boolean(errosCampos[`rendimento-${index}`])}
                    />
                    {errosCampos[`rendimento-${index}`] && (
                      <small className="auth-field-error">{errosCampos[`rendimento-${index}`]}</small>
                    )}
                    <button
                      className="employer-remove"
                      type="button"
                      onClick={() => removeEmployer(index)}
                    >
                      Remover entidade
                    </button>
                  </fieldset>
                ))}
              </section>
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
                aria-invalid={Boolean(errosCampos.email)}
                aria-describedby={errosCampos.email ? 'email-error' : undefined}
              />
              {errosCampos.email && <small className="auth-field-error" id="email-error">{errosCampos.email}</small>}
              <div className="auth-password-field">
                <input
                  type={mostrarSenha ? 'text' : 'password'}
                  name="senha"
                  placeholder="Senha"
                  aria-label="Senha"
                  value={formData.senha}
                  onChange={handleChange}
                  required
                  autoComplete="new-password"
                  className="auth-input"
                  aria-invalid={Boolean(errosCampos.senha)}
                  aria-describedby={errosCampos.senha ? 'senha-error' : undefined}
                />
                <button
                  type="button"
                  className="auth-password-toggle"
                  onClick={() => setMostrarSenha((mostrar) => !mostrar)}
                  aria-label={mostrarSenha ? 'Ocultar senha' : 'Mostrar senha'}
                  aria-pressed={mostrarSenha}
                >
                  {mostrarSenha ? <EyeOffIcon /> : <EyeIcon />}
                </button>
              </div>
              {errosCampos.senha && <small className="auth-field-error" id="senha-error">{errosCampos.senha}</small>}
              <div className="auth-password-field">
                <input
                  type={mostrarConfirmacao ? 'text' : 'password'}
                  name="confirmarSenha"
                  placeholder="Confirme a senha"
                  aria-label="Confirme a senha"
                  value={formData.confirmarSenha}
                  onChange={handleChange}
                  required
                  autoComplete="new-password"
                  className="auth-input"
                  aria-invalid={Boolean(errosCampos.confirmarSenha)}
                  aria-describedby={errosCampos.confirmarSenha ? 'confirmar-senha-error' : undefined}
                />
                <button
                  type="button"
                  className="auth-password-toggle"
                  onClick={() => setMostrarConfirmacao((mostrar) => !mostrar)}
                  aria-label={mostrarConfirmacao ? 'Ocultar confirmação de senha' : 'Mostrar confirmação de senha'}
                  aria-pressed={mostrarConfirmacao}
                >
                  {mostrarConfirmacao ? <EyeOffIcon /> : <EyeIcon />}
                </button>
              </div>
              {errosCampos.confirmarSenha && (
                <small className="auth-field-error" id="confirmar-senha-error">{errosCampos.confirmarSenha}</small>
              )}

              {erro && <p className="auth-error" role="alert">{erro}</p>}
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

function EyeIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12Z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  );
}

function EyeOffIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="m3 3 18 18M10.6 10.6a2 2 0 0 0 2.8 2.8" />
      <path d="M9.9 5.2A9.8 9.8 0 0 1 12 5c6.4 0 10 7 10 7a14.6 14.6 0 0 1-3.2 4.1M6.2 6.2C3.5 8 2 12 2 12s3.6 7 10 7a9.8 9.8 0 0 0 4.1-.9" />
    </svg>
  );
}