import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import logoRentCar from '../assets/LogoRentCar.png';
import { deleteMyProfile, getMyProfile, updateMyProfile } from '../services/client';

const EMPTY_PROFILE = {
  email: '',
  nome: '',
  cpf: '',
  rg: '',
  morada: '',
  profissao: '',
  entidadesEmpregadoras: [],
};

export default function ClientProfile() {
  const [profile, setProfile] = useState(EMPTY_PROFILE);
  const [formData, setFormData] = useState(EMPTY_PROFILE);
  const [carregando, setCarregando] = useState(true);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState('');
  const [mensagem, setMensagem] = useState('');
  const [editando, setEditando] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    let active = true;

    getMyProfile()
      .then((data) => {
        if (active) {
          setProfile(data);
          setFormData({
            ...data,
            entidadesEmpregadoras: data.entidadesEmpregadoras || [],
          });
        }
      })
      .catch((error) => {
        if (active) {
          setErro(error.message);
        }
      })
      .finally(() => {
        if (active) {
          setCarregando(false);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  const handleChange = (event) => {
    const { name, value } = event.target;
    const sanitizedValue = name === 'cpf'
      ? value.replace(/\D/g, '')
      : name === 'rg'
        ? value.replace(/[^a-zA-Z0-9]/g, '')
        : value;

    setFormData((current) => ({ ...current, [name]: sanitizedValue }));
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

  const handleUpdate = async (event) => {
    event.preventDefault();
    setErro('');
    setMensagem('');
    setSalvando(true);

    try {
      const updatedProfile = await updateMyProfile({
        ...formData,
        entidadesEmpregadoras: formData.entidadesEmpregadoras.map((entidade) => ({
          ...entidade,
          rendimentoAuferido: Number(entidade.rendimentoAuferido),
        })),
      });
      setProfile(updatedProfile);
      setFormData({
        ...updatedProfile,
        entidadesEmpregadoras: updatedProfile.entidadesEmpregadoras || [],
      });
      setEditando(false);
      setMensagem('Seus dados foram atualizados.');
    } catch (error) {
      setErro(error.message);
    } finally {
      setSalvando(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Tem certeza que deseja excluir sua conta? Essa ação não pode ser desfeita.')) {
      return;
    }

    setErro('');
    try {
      await deleteMyProfile();
      navigate('/', {
        replace: true,
        state: { message: 'Sua conta foi excluída.' },
      });
    } catch (error) {
      setErro(error.message);
    }
  };

  const cancelEdit = () => {
    setFormData(profile);
    setEditando(false);
    setErro('');
    setMensagem('');
  };

  return (
    <main className="auth-page">
      <section className="auth-card auth-card--register" aria-label="Perfil do cliente">
        <aside className="auth-brand">
          <img className="auth-brand__logo" src={logoRentCar} alt="RentCar - aluguel de veículos" />
        </aside>

        <div className="auth-content">
          <div className="auth-content__inner">
            <span className="auth-eyebrow">RENT CAR</span>
            <h1 className="auth-title auth-title--register">Meu perfil</h1>

            {carregando && <p className="auth-home__description">Carregando seus dados...</p>}
            {!carregando && erro && <p className="auth-error" role="alert">{erro}</p>}
            {mensagem && <p className="auth-success auth-success--inline" role="status">{mensagem}</p>}

            {!carregando && profile.email && (
              editando ? (
                <form className="auth-form client-profile-form" onSubmit={handleUpdate}>
                  <label>
                    Nome completo
                    <input className="auth-input" name="nome" value={formData.nome} onChange={handleChange} required />
                  </label>
                  <label>
                    CPF
                    <input className="auth-input" name="cpf" value={formData.cpf} onChange={handleChange} inputMode="numeric" required />
                  </label>
                  <label>
                    RG
                    <input className="auth-input" name="rg" value={formData.rg} onChange={handleChange} required />
                  </label>
                  <label>
                    Morada
                    <input className="auth-input" name="morada" value={formData.morada} onChange={handleChange} required />
                  </label>
                  <label>
                    Profissão
                    <input className="auth-input" name="profissao" value={formData.profissao} onChange={handleChange} required />
                  </label>
                  <section className="employers-form" aria-labelledby="profile-employers-title">
                    <div className="employers-form__heading">
                      <div>
                        <h2 id="profile-employers-title">Entidades empregadoras</h2>
                        <p>Adicione até três fontes de rendimento.</p>
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
                        />
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
                        />
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
                  <label>
                    E-mail
                    <input className="auth-input" type="email" value={profile.email} readOnly />
                  </label>
                  <div className="client-profile__actions">
                    <button className="auth-submit" type="submit" disabled={salvando}>
                      {salvando ? 'Salvando...' : 'Salvar alterações'}
                    </button>
                    <button className="auth-submit auth-submit--secondary" type="button" onClick={cancelEdit}>
                      Cancelar
                    </button>
                  </div>
                </form>
              ) : (
                <>
                  <dl className="client-profile__details">
                    <ProfileField label="Nome completo" value={profile.nome} />
                    <ProfileField label="CPF" value={profile.cpf} />
                    <ProfileField label="RG" value={profile.rg} />
                    <ProfileField label="Morada" value={profile.morada} />
                    <ProfileField label="Profissão" value={profile.profissao} />
                    <ProfileField label="E-mail" value={profile.email} />
                  </dl>
                  <section className="client-employers" aria-labelledby="profile-employers-title">
                    <h2 id="profile-employers-title">Entidades empregadoras</h2>
                    {profile.entidadesEmpregadoras?.length ? (
                      <ul>
                        {profile.entidadesEmpregadoras.map((entidade, index) => (
                          <li key={`${entidade.nome}-${index}`}>
                            <span>{entidade.nome}</span>
                            <strong>
                              {new Intl.NumberFormat('pt-BR', {
                                style: 'currency',
                                currency: 'BRL',
                              }).format(entidade.rendimentoAuferido)}
                            </strong>
                          </li>
                        ))}
                      </ul>
                    ) : (
                      <p>Nenhuma entidade empregadora cadastrada.</p>
                    )}
                  </section>
                  <div className="client-profile__actions">
                    <Link to="/dashboard" className="auth-submit auth-submit--secondary auth-home__link">
                      Voltar à dashboard
                    </Link>
                    <button className="auth-submit" type="button" onClick={() => setEditando(true)}>
                      Editar perfil
                    </button>
                    <button className="auth-submit auth-submit--danger" type="button" onClick={handleDelete}>
                      Excluir conta
                    </button>
                  </div>
                </>
              )
            )}

            {!carregando && !profile.email && (
              <p className="auth-footer"><Link to="/login">Voltar para o login</Link></p>
            )}
          </div>
        </div>
      </section>
    </main>
  );
}

function ProfileField({ label, value }) {
  return (
    <div className="client-profile__field">
      <dt>{label}</dt>
      <dd>{value || '—'}</dd>
    </div>
  );
}
