import { useCallback, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { LoginApiError } from '../api/auth'
import SmartCaptchaWidget from '../components/SmartCaptchaWidget'

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [captchaRequired, setCaptchaRequired] = useState(false)
  const [captchaToken, setCaptchaToken] = useState<string | undefined>()
  const [captchaResetKey, setCaptchaResetKey] = useState(0)
  const [captchaError, setCaptchaError] = useState('')

  const handleCaptchaToken = useCallback((token: string) => {
    setCaptchaError('')
    setCaptchaToken(token)
  }, [])

  const handleCaptchaError = useCallback((message: string) => {
    setCaptchaToken(undefined)
    setCaptchaError(message)
    setCaptchaResetKey((value) => value + 1)
  }, [])

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setLoading(true)
    const submittedCaptchaToken = Boolean(captchaToken)

    try {
      await login(email, password, captchaToken)
      navigate('/dashboard')
    } catch (caughtError) {
      if (submittedCaptchaToken) {
        setCaptchaToken(undefined)
        setCaptchaResetKey((value) => value + 1)
      }
      if (caughtError instanceof LoginApiError && caughtError.status === 428) {
        setCaptchaRequired(true)
        if (!submittedCaptchaToken) {
          setCaptchaToken(undefined)
          setCaptchaResetKey((value) => value + 1)
        }
        setError('Для продолжения пройди дополнительную проверку.')
      } else if (caughtError instanceof LoginApiError && caughtError.code === 'CAPTCHA_INVALID') {
        if (!submittedCaptchaToken) {
          setCaptchaToken(undefined)
          setCaptchaResetKey((value) => value + 1)
        }
        setError('Проверка SmartCaptcha не пройдена. Попробуй ещё раз.')
      } else if (caughtError instanceof LoginApiError && caughtError.code === 'CAPTCHA_UNAVAILABLE') {
        setError('Проверка временно недоступна. Попробуй позже.')
      } else {
        setError('Invalid email or password')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="auth-page">
      <div className="auth-card">
        <div className="auth-brand">
          <div className="auth-portrait-wrap">
            <img
              className="auth-brand-icon"
              src="/branding/portrait/app-icon-192.png"
              alt=""
              aria-hidden="true"
            />
          </div>
          <h1 className="auth-wordmark" aria-label="EvaGita">
            <span className="auth-wordmark-eva">Eva</span><span className="auth-wordmark-gita">Gita</span>
          </h1>
          <p className="auth-tagline">ПЛАНЫ <span>·</span> ПРОЕКТЫ <span>·</span> ВДОХНОВЕНИЕ</p>
        </div>
        <h2>С возвращением</h2>
        <p className="auth-intro">Войди в свой аккаунт, чтобы продолжить.</p>

        <form onSubmit={handleSubmit}>
          <label>
            Электронная почта
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
          </label>

          <SmartCaptchaWidget
            visible={captchaRequired}
            resetKey={captchaResetKey}
            onToken={handleCaptchaToken}
            onError={handleCaptchaError}
          />

          {captchaError && <p className="form-error" role="alert">{captchaError}</p>}

          <label>
            Пароль
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </label>

          {error && <p className="form-error" role="alert">Не удалось войти. Проверь почту и пароль.</p>}

          <button type="submit" disabled={loading}>
            {loading ? 'Входим…' : 'Войти'}
          </button>
        </form>

        <p>
          Впервые здесь?{' '}
          <Link to="/register">Создать аккаунт</Link>
        </p>
      </div>
    </main>
  )
}
