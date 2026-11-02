import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import logoRentCar from '../assets/LogoRentCar.png';
import { createAgent, listAgents, updateAgent } from '../services/admin';
import { getCurrentSession } from '../services/auth';

const EMPTY_AGENT = {
  nome: '',
  email: '',
  tipo: 'EMPRESA',
  organizacao: '',
  senha: '',
};

export default function AdminDashboard() {
  const [agent, setAgent] = useState(EMPTY_AGENT);
  const [agents, setAgents] = useState([]);
  const [session, setSession] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [listError, setListError] = useState('');
  const location = useLocation();
  const [message, setMessage] = useState(location.state?.message || '');
  const navigate = useNavigate();

  useEffect(() => {
    let active = true;

    getCurrentSession()
      .then(async (currentSession) => {
        if (!active) {
          return;
        }
        if (currentSession.tipoUsuario !== 'ADMIN') {
          navigate(
            currentSession.tipoUsuario === 'AGENTE' ? '/agente' : '/dashboard',
            { replace: true },
          );
          return;
        }
        setSession(currentSession);
        try {
          const registeredAgents = await listAgents();
          if (active) {
            setAgents(registeredAgents);
          }
        } catch (requestError) {
          if (active) {
            setListError(requestError.message);
          }
        }
      })
      .catch((requestError) => {
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
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [navigate]);

  const handleChange = (event) => {
    setAgent((current) => ({ ...current, [event.target.name]: event.target.value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setMessage('');
    setSaving(true);

    try {
      const createdAgent = await createAgent(agent);
      setAgents((current) => [...current, createdAgent].sort((a, b) => a.nome.localeCompare(b.nome)));
      setListError('');
      setAgent(EMPTY_AGENT);
      setMessage(`Agente ${createdAgent.nome} (${createdAgent.email}) cadastrado.`);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <main className="auth-page">
      <section className="auth-card auth-card--register" aria-label="Administração do RentCar">
        <aside className="auth-brand">
          <img className="auth-brand__logo" src={logoRentCar} alt="RentCar - aluguel de veículos" />
        </aside>

        <div className="auth-content">
          <div className="auth-content__inner">
            <span className="auth-eyebrow">RENT CAR · ADMINISTRAÇÃO</span>
            <h1 className="auth-title auth-title--register">Administração de agentes</h1>
            <p className="auth-home__description">
              {session
                ? 'Cadastre novos agentes, consulte a lista e atualize seus perfis.'
                : 'Verificando o acesso administrativo...'}
            </p>

            {error && <p className="auth-error" role="alert">{error}</p>}
            {message && <p className="auth-success" role="status">{message}</p>}

            {session && (
              <>
                <h2 className="orders-list__title admin-section-title">Cadastrar agente</h2>
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
                    Senha inicial (mínimo de 8 caracteres)
                    <input
                      className="auth-input"
                      type="password"
                      name="senha"
                      value={agent.senha}
                      onChange={handleChange}
                      autoComplete="new-password"
                      minLength={8}
                      required
                    />
                  </label>
                  <button className="auth-submit" type="submit" disabled={saving || loading}>
                    {saving ? 'Cadastrando...' : 'Cadastrar agente'}
                  </button>
                </form>

                <section className="admin-agents" aria-labelledby="admin-agents-heading">
                  <h2 id="admin-agents-heading" className="orders-list__title admin-section-title">
                    Agentes cadastrados
                  </h2>
                  {listError && <p className="auth-error" role="alert">{listError}</p>}
                  {!listError && agents.length === 0 && (
                    <p className="orders-list__empty">
                      {loading ? 'Carregando agentes...' : 'Nenhum agente cadastrado.'}
                    </p>
                  )}
                  {agents.map((registeredAgent) => (
                    <article className="admin-agent" key={registeredAgent.id}>
                      <div className="admin-agent__summary">
                        <div>
                          <h3>{registeredAgent.nome}</h3>
                          <p>{registeredAgent.email}</p>
                          <span className="admin-agent__type">
                            {registeredAgent.tipo === 'BANCO' ? 'Agente de banco' : 'Agente de empresa'}
                            {' · '}
                            {registeredAgent.organizacao || 'Instituição não informada'}
                          </span>
                        </div>
                        <Link
                          className="auth-submit auth-submit--danger admin-agent__edit-link"
                          to={`/admin/agentes/${registeredAgent.id}/editar`}
                        >
                          Editar perfil
                        </Link>
                      </div>
                    </article>
                  ))}
                </section>
              </>
            )}

            <p className="auth-footer"><Link to="/">Voltar ao início</Link></p>
          </div>
        </div>
      </section>
    </main>
  );
}
