import { useEffect, useState } from 'react'
import {
  getNotifications,
  getUnreadNotificationsCount,
  markNotificationAsRead,
} from '../api/notifications'
import type { Notification } from '../types/notification'

type NotificationFilter = 'ALL' | 'UNREAD'

const notificationTypeLabels: Record<string, string> = {
  TASK_CREATED: 'Task created',
  TASK_UPDATED: 'Task updated',
  TASK_COMPLETED: 'Task completed',
  TASK_OVERDUE: 'Task overdue',
}

export default function NotificationsPage() {

  const [notifications, setNotifications] = useState<Notification[]>([])
  const [unreadCount, setUnreadCount] = useState(0)
  const [filter, setFilter] = useState<NotificationFilter>('ALL')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const loadNotifications = async () => {
    try {
      setLoading(true)
      setError('')

      const [data, count] = await Promise.all([
        getNotifications(),
        getUnreadNotificationsCount(),
      ])

      setNotifications(data)
      setUnreadCount(count)
    } catch {
      setError('Failed to load notifications.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadNotifications()
  }, [])

  const handleMarkAsRead = async (id: number) => {
    try {
      const updated = await markNotificationAsRead(id)

      setNotifications((current) =>
        current.map((notification) =>
          notification.id === updated.id ? updated : notification,
        ),
      )

      setUnreadCount((current) => Math.max(0, current - 1))
    } catch {
      setError('Failed to mark notification as read.')
    }
  }

  const visibleNotifications =
    filter === 'UNREAD'
      ? notifications.filter((notification) => !notification.read)
      : notifications

  return (
    <main className="notifications-page">


      <section className="notifications-content">
        <header className="notifications-header">
          <div>
            <h1>Notifications</h1>
            <p>Stay up to date with your tasks.</p>
          </div>

          <strong className="unread-count">
            Unread: {unreadCount}
          </strong>
        </header>

        {error && <p className="form-error">{error}</p>}

        <section className="notification-filters">
          <button
            type="button"
            className={filter === 'ALL' ? 'active' : ''}
            onClick={() => setFilter('ALL')}
          >
            All
          </button>

          <button
            type="button"
            className={filter === 'UNREAD' ? 'active' : ''}
            onClick={() => setFilter('UNREAD')}
          >
            Unread
          </button>
        </section>

        {loading ? (
          <p>Loading notifications...</p>
        ) : visibleNotifications.length === 0 ? (
          <p>No notifications found.</p>
        ) : (
          <section className="notifications-list">
            {visibleNotifications.map((notification) => (
              <article
                key={notification.id}
                className={`notification-card ${
                  notification.read ? 'read' : 'unread'
                }`}
              >
                <div className="notification-main">
                  <div className="notification-top">
                    <span className="notification-type">
                      {notificationTypeLabels[notification.type] ??
                        notification.type}
                    </span>

                    {!notification.read && (
                      <span className="notification-status">
                        Unread
                      </span>
                    )}
                  </div>

                  <p>{notification.message}</p>

                  <time dateTime={notification.createdAt}>
                    {new Date(
                      notification.createdAt,
                    ).toLocaleString()}
                  </time>
                </div>

                {!notification.read && (
                  <button
                    type="button"
                    onClick={() =>
                      void handleMarkAsRead(notification.id)
                    }
                  >
                    Mark as read
                  </button>
                )}
              </article>
            ))}
          </section>
        )}
      </section>
    </main>
  )
}
