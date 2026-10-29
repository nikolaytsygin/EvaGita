import { useEffect, useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { getUnreadNotificationsCount } from '../api/notifications'

export default function AppLayout() {
  const { logout } = useAuth()
  const navigate = useNavigate()
  const [unreadCount, setUnreadCount] = useState(0)

  useEffect(() => {
    void getUnreadNotificationsCount().then(setUnreadCount).catch(() => setUnreadCount(0))
  }, [])

  return (
    <div className="app-shell">
      <nav className="main-nav">
        <NavLink to="/dashboard" className="brand-link">
          <img className="brand-icon" src="/branding/portrait/app-icon-192.png" alt="" aria-hidden="true" />
          <span>EvaGita</span>
        </NavLink>
        <div className="nav-links">
          <NavLink to="/dashboard">Обзор</NavLink>
          <NavLink to="/tasks">Задачи</NavLink>
          <NavLink to="/projects">Проекты</NavLink>
          <NavLink to="/tags">Теги</NavLink>
          <NavLink to="/notifications">
            Уведомления{unreadCount > 0 && <span className="notification-badge">{unreadCount}</span>}
          </NavLink>
        </div>
        <div className="nav-actions">
          <input className="nav-search" type="search" placeholder="Поиск задач" aria-label="Поиск задач" />
          <button type="button" onClick={() => { logout(); navigate('/login') }}>Профиль⌄</button>
        </div>
      </nav>
      <Outlet />
    </div>
  )
}
