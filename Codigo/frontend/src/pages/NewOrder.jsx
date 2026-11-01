import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import logoRentCar from '../assets/LogoRentCar.png';
import { createOrder, getAutomoveis, getMyOrders } from '../services/orders';

function localDateValue(date = new Date()) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function nextDateValue(value) {
  if (!value) {
    return localDateValue();
  }
  const [year, month, day] = value.split('-').map(Number);
  return localDateValue(new Date(year, month - 1, day + 1));
}

export default function NewOrder() {
  const [automoveis, setAutomoveis] = useState([]);
  const [formData, setFormData] = useState({
    automovelId: '',
    dataInicio: '',
    dataFim: '',
  });
  const [carregando, setCarregando] = useState(true);
  const [sessaoAtiva, setSessaoAtiva] = useState(null);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    let active = true;

    Promise.allSettled([getAutomoveis(), getMyOrders()])
      .then(([carsResult, ordersResult]) => {
        if (!active) {
          return;
        }

        if (carsResult.status === 'fulfilled') {
          setAutomoveis(carsResult.value);
        } else {
          setErro(carsResult.reason.message);
        }

        if (ordersResult.status === 'fulfilled') {
          setSessaoAtiva(true);
        } else if (ordersResult.reason.status === 401 || ordersResult.reason.status === 403) {
          setSessaoAtiva(false);
        } else if (carsResult.status === 'fulfilled') {
          setErro(ordersResult.reason.message);
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
    setFormData((current) => ({ ...current, [event.target.name]: event.target.value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setErro('');
    setEnviando(true);

    try {
      await createOrder({
        ...formData,
        automovelId: Number(formData.automovelId),
      });
      navigate('/pedidos', {
        replace: true,
        state: { message: 'Pedido enviado para análise.' },
      });
    } catch (error) {
      setErro(error.message);
    } finally {
      setEnviando(false);
    }
  };

  return (
    <main className="auth-page">
      <section className="auth-card auth-card--register orders-card" aria-label="Solicitar aluguel">
        <aside className="auth-brand">
          <img className="auth-brand__logo" src={logoRentCar} alt="RentCar - aluguel de veículos" />
        </aside>

        <div className="auth-content">
          <div className="auth-content__inner">
            <span className="auth-eyebrow">RENT CAR</span>
            <h1 className="auth-title auth-title--register">Solicitar aluguel</h1>
            <p className="auth-home__description">
              Escolha um automóvel e o período desejado. Seu pedido será enviado para análise.
            </p>

            {erro && <p className="auth-error" role="alert">{erro}</p>}
            {!carregando && sessaoAtiva === false && (
              <p className="auth-error" role="alert">
                Para solicitar um aluguel, <Link to="/login">entre na sua conta</Link>.
              </p>
            )}

            <form className="auth-form orders-form" onSubmit={handleSubmit}>
              <label>
                Automóvel
                <select
                  className="auth-input"
                  name="automovelId"
                  value={formData.automovelId}
                  onChange={handleChange}
                  required
                  disabled={carregando || automoveis.length === 0}
                >
                  <option value="" disabled>
                    {automoveis.length ? 'Selecione um automóvel' : 'Nenhum automóvel disponível'}
                  </option>
                  {automoveis.map((automovel) => (
                    <option key={automovel.id} value={automovel.id}>
                      {automovel.marca} {automovel.modelo} ({automovel.ano}) · {automovel.placa}
                    </option>
                  ))}
                </select>
              </label>
              <div className="orders-form__dates">
                <label>
                  Data inicial
                  <input
                    className="auth-input"
                    type="date"
                    name="dataInicio"
                    value={formData.dataInicio}
                    onChange={handleChange}
                    min={localDateValue()}
                    required
                  />
                </label>
                <label>
                  Data final
                  <input
                    className="auth-input"
                    type="date"
                    name="dataFim"
                    value={formData.dataFim}
                    onChange={handleChange}
                    min={nextDateValue(formData.dataInicio)}
                    required
                  />
                </label>
              </div>
              <button
                className="auth-submit"
                type="submit"
                disabled={enviando || carregando || !sessaoAtiva || automoveis.length === 0}
              >
                {enviando ? 'Enviando...' : 'Enviar pedido'}
              </button>
            </form>

            <p className="auth-footer"><Link to="/dashboard">Voltar à dashboard</Link></p>
          </div>
        </div>
      </section>
    </main>
  );
}
