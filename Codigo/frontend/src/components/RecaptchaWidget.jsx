import React, { useEffect, useRef, useState } from 'react';
import { getRecaptchaSiteKey } from '../services/auth';

let recaptchaScriptPromise;

function loadRecaptchaScript() {
  if (window.grecaptcha?.render) {
    return Promise.resolve(window.grecaptcha);
  }

  if (!recaptchaScriptPromise) {
    recaptchaScriptPromise = new Promise((resolve, reject) => {
      let script = document.querySelector('script[data-recaptcha-api]');
      const shouldAppend = !script;

      const onLoad = () => {
        window.grecaptcha.ready(() => resolve(window.grecaptcha));
      };

      const onError = () => {
        recaptchaScriptPromise = undefined;
        reject(new Error('Não foi possível carregar o reCAPTCHA.'));
      };

      if (!script) {
        script = document.createElement('script');
        script.src = 'https://www.google.com/recaptcha/api.js?render=explicit';
        script.async = true;
        script.defer = true;
        script.dataset.recaptchaApi = 'true';
      }

      script.addEventListener('load', onLoad, { once: true });
      script.addEventListener('error', onError, { once: true });
      if (shouldAppend) {
        document.head.appendChild(script);
      }
    });
  }

  return recaptchaScriptPromise;
}

export default function RecaptchaWidget({ onChange, onRequiredChange }) {
  const containerRef = useRef(null);
  const [erro, setErro] = useState('');
  const [recaptchaEnabled, setRecaptchaEnabled] = useState(null);

  useEffect(() => {
    let active = true;
    let widgetId;

    async function renderWidget() {
      try {
        const siteKey = await getRecaptchaSiteKey();
        if (!active) {
          return;
        }
        if (!siteKey) {
          setRecaptchaEnabled(false);
          onRequiredChange(false);
          return;
        }

        const recaptcha = await loadRecaptchaScript();

        if (!active || !containerRef.current) {
          return;
        }

        setRecaptchaEnabled(true);
        onRequiredChange(true);
        widgetId = recaptcha.render(containerRef.current, {
          sitekey: siteKey,
          callback: (token) => onChange(token),
          'expired-callback': () => onChange(''),
          'error-callback': () => {
            onChange('');
            setErro('Falha ao carregar o reCAPTCHA. Verifique sua conexão e tente novamente.');
          },
        });
        setErro('');
      } catch (error) {
        if (active) {
          setErro(error.message || 'Não foi possível iniciar o reCAPTCHA.');
        }
      }
    }

    renderWidget();

    return () => {
      active = false;
      if (widgetId !== undefined && window.grecaptcha?.reset) {
        window.grecaptcha.reset(widgetId);
      }
    };
  }, [onChange, onRequiredChange]);

  return (
    <div className="auth-captcha">
      {recaptchaEnabled === false
        ? <p className="auth-captcha__disabled">reCAPTCHA desativado para este ambiente.</p>
        : <div ref={containerRef} />}
      {erro && <p className="auth-error" role="alert">{erro}</p>}
    </div>
  );
}
