import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import logoRentCar from '../assets/LogoRentCar.png';
import { getAgent, updateAgent } from '../services/admin';
import { getCurrentSession } from '../services/auth';

const EMPTY_AGENT = {
  nome: '',
  email: '',
  tipo: 'EMPRESA',
  organizacao: '',
  senha: '',
};

export default function AdminAgentEdit() {
  const { id } = useParams();
  const [agent, setAgent] = useState(EMPTY_AGENT);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    let active = true;

    async function loadAgent() {
      try {
        const session = await getCurrentSession();
        if (session.tipoUsuario !== 'ADMIN') {
          navigate(
            session.tipoUsuario === 'AGENTE' ? '/agente' : '/dashboard',
            { replace: true },
          );
          return;
        }

        const registeredAgent = await getAgent(id);
        if (active) {
          setAgent({
            nome: registeredAgent.nome || '',
            email: registeredAgent.email || '',
            tipo: registeredAgent.tipo || 'EMPRESA',
            organizacao: registeredAgent.organizacao || '',
            senha: '',
          });
        }
      } catch (requestError) {
        if (!active) {
          return;
        }
        if (requestError.status === 401 || requestError.status === 403) {
          navigate('/login', {
            replace: true,
            state: { message: 'Entre com uma conta administradora para continuar.' },
          });
          return;
        }
        setError(requestError.message);
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadAgent();
    return () => {
      active = false;
    };
  }, [id, navigate]);

  const handleChange = (event) => {
    setAgent((current) => ({ ...current, [event.target.name]: event.target.value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setSaving(true);

    try {
      const updatedAgent = await updateAgent(id, agent);
      navigate('/admin', {
        replace: true,
        state: { message: `Perfil de ${updatedAgent.nome} atualizado.` },
      });
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <main className="auth-page">
      <section className="auth-card auth-card--register" aria-label="Editar perfil de agente">
        <aside className="auth-brand">
          <img className="auth-brand__logo" src={logoRentCar} alt="RentCar - aluguel de veículos" />
        </aside>

        <div className="auth-content">
          <div className="auth-content__inner">
            <span className="auth-eyebrow">RENT CAR · ADMINISTRAÇÃO</span>
            <h1 className="auth-title auth-title--register">Editar perfil do agente</h1>
            <p className="auth-home__description">
              Atualize os dados de acesso e a instituição do agente.
            </p>

            {error && <p className="auth-error" role="alert">{error}</p>}
            {loading ? (
              <p className="auth-home__description">Carregando perfil do agente...</p>
            ) : !error && (
              <form className="auth-form client-profile-form" onSubmit={handleSubmit}>
                <label>
                  Nome completo
                  <input
                    className="auth-input"
                    name="nome"
                    value={agent.nome}
                    onChange={handleChange}
                    autoComplete="name"
                    required
                  />
                </label>
                <label>
                  E-mail de acesso
                  <input
                    className="auth-input"
                    type="email"
                    name="email"
                    value={agent.email}
                    onChange={handleChange}
                    autoComplete="email"
                    required
                  />
                </label>
                <label>
                  Tipo de agente
                  <select
                    className="auth-input"
                    name="tipo"
                    value={agent.tipo}
                    onChange={handleChange}
                    required
                  >
                    <option value="EMPRESA">Empresa</option>
                    <option value="BANCO">Banco</option>
                  </select>
                </label>
                <label>
                  {agent.tipo === 'BANCO' ? 'Nome do banco' : 'Nome da empresa'}
                  <input
                    className="auth-input"
                    name="organizacao"
                    value={agent.organizacao}
                    onChange={handleChange}
                    required
                  />
                </label>
                <label>
                  Nova senha (opcional, mínimo de 8 caracteres)
                  <input
                    className="auth-input"
                    type="password"
                    name="senha"
                    value={agent.senha}
                    onChange={handleChange}
                    autoComplete="new-password"
                    minLength={8}
                  />
                </label>
                <div className="admin-agent__actions">
                  <button className="auth-submit" type="submit" disabled={saving}>
                    {saving ? 'Salvando...' : 'Salvar perfil'}
                  </button>
                  <Link className="auth-submit auth-submit--secondary" to="/admin">
                    Cancelar
                  </Link>
                </div>
              </form>
            )}

            <p className="auth-footer"><Link to="/admin">Voltar aos agentes</Link></p>
          </div>
        </div>
      </section>
    </main>
  );
}
