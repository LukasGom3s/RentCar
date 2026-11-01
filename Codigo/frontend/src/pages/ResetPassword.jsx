import React, { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import logoRentCar from '../assets/LogoRentCar.png';
import { submitAuthRequest } from '../services/auth';

export default function ResetPassword() {
  const [searchParams] = useSearchParams();
  const [senha, setSenha] = useState('');
  const [confirmarSenha, setConfirmarSenha] = useState('');
  const [erro, setErro] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [concluido, setConcluido] = useState(false);
  const navigate = useNavigate();
  const token = searchParams.get('token') || '';

  const handleSubmit = async (event) => {
    event.preventDefault();
    setErro('');

    if (!token) {
      setErro('O link de redefinição é inválido ou está incompleto.');
      return;
    }
    if (senha !== confirmarSenha) {
      setErro('As senhas não coincidem.');
      return;
    }

    setEnviando(true);
    try {
      await submitAuthRequest('resetpassword', { token, senha, confirmarSenha });
      setConcluido(true);
      window.setTimeout(() => navigate('/login', { replace: true }), 2500);
    } catch (error) {
      setErro(error.message);
    } finally {
      setEnviando(false);
    }
  };

  return (
    <main className="auth-page">
      <section className="auth-card" aria-label="Redefinição de senha">
        <aside className="auth-brand">
          <img className="auth-brand__logo" src={logoRentCar} alt="RentCar - aluguel de veículos" />
        </aside>

        <div className="auth-content">
          <div className="auth-content__inner">
            <span className="auth-eyebrow">RENT CAR</span>
            <h1 className="auth-title">Criar nova senha</h1>
            {concluido ? (
              <>
                <p className="auth-success auth-success--inline" role="status">
                  Senha alterada com sucesso. Redirecionando para o login...
                </p>
                <p className="auth-footer"><Link to="/login">Ir para o login</Link></p>
              </>
            ) : (
              <form className="auth-form" onSubmit={handleSubmit}>
                <label className="visually-hidden" htmlFor="new-password">Nova senha</label>
                <input
                  className="auth-input"
                  id="new-password"
                  type="password"
                  name="senha"
                  placeholder="Nova senha"
                  value={senha}
                  onChange={(event) => setSenha(event.target.value)}
                  autoComplete="new-password"
                  required
                />
                <label className="visually-hidden" htmlFor="confirm-new-password">Confirme a nova senha</label>
                <input
                  className="auth-input"
                  id="confirm-new-password"
                  type="password"
                  name="confirmarSenha"
                  placeholder="Confirme a nova senha"
                  value={confirmarSenha}
                  onChange={(event) => setConfirmarSenha(event.target.value)}
                  autoComplete="new-password"
                  required
                />
                {erro && <p className="auth-error" role="alert">{erro}</p>}
                <button className="auth-submit" type="submit" disabled={enviando || !token}>
                  {enviando ? 'Salvando...' : 'Redefinir senha'}
                </button>
              </form>
            )}
            {!concluido && (
              <p className="auth-footer"><Link to="/login">Voltar para o login</Link></p>
            )}
          </div>
        </div>
      </section>
    </main>
  );
}
