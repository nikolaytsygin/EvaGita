import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'

export default function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()

  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setLoading(true)

    try {
      await register(username, email, password)
      navigate('/login')
    } catch {
      setError('Registration failed. Check your data and try again.')
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
        <h2>Создай своё пространство</h2>
        <p className="auth-intro">Всего несколько шагов — и можно планировать.</p>

        <form onSubmit={handleSubmit}>
          <label>
            Имя пользователя
            <input
              type="text"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              required
            />
          </label>

          <label>
            Электронная почта
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
          </label>

          <label>
            Пароль
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </label>

          {error && <p className="form-error" role="alert">Не удалось создать аккаунт. Проверь данные и попробуй ещё раз.</p>}

          <button type="submit" disabled={loading}>
            {loading ? 'Создаём аккаунт…' : 'Создать аккаунт →'}
          </button>
        </form>

        <p>
          Уже есть аккаунт?{' '}
          <Link to="/login">Войти</Link>
        </p>
      </div>
    </main>
  )
}
