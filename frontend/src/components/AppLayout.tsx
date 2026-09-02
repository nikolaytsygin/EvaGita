import { useEffect, useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { getUnreadNotificationsCount } from '../api/notifications'

export default function AppLayout() {
  const { logout } = useAuth()
  const navigate = useNavigate()
  const [unreadCount, setUnreadCount] = useState(0)

  const loadUnreadCount = async () => {
    try {
      const count = await getUnreadNotificationsCount()
      setUnreadCount(count)
    } catch {
      setUnreadCount(0)
    }
  }

  useEffect(() => {
    void loadUnreadCount()
  }, [])

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const navClass = ({ isActive }: { isActive: boolean }) =>
    isActive ? 'active' : ''

  return (
    <>
      <nav className="main-nav">
        <div className="nav-links">
          <NavLink to="/dashboard" className={navClass}>
            Dashboard
          </NavLink>

          <NavLink to="/tasks" className={navClass}>
            Tasks
          </NavLink>

          <NavLink to="/projects" className={navClass}>
            Projects
          </NavLink>

          <NavLink to="/tags" className={navClass}>
            Tags
          </NavLink>

          <NavLink to="/notifications" className={navClass}>
            Notifications
            {unreadCount > 0 && (
              <span className="notification-badge">
                {unreadCount}
              </span>
            )}
          </NavLink>
        </div>

        <button type="button" onClick={handleLogout}>
          Logout
        </button>
      </nav>

      <Outlet />
    </>
  )
}
