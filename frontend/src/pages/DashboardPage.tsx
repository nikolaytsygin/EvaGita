import { useEffect, useState } from 'react'
import { getDashboard } from '../api/dashboard'
import { getProjects } from '../api/projects'
import { getTags } from '../api/tags'
import { useAuth } from '../hooks/useAuth'
import type { Dashboard } from '../types/dashboard'
import './dashboard.css'

interface DashboardData extends Dashboard {
  projectsCount: number
  tagsCount: number
}

export default function DashboardPage() {
  const { user } = useAuth()
  const [dashboard, setDashboard] = useState<DashboardData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    void Promise.all([getDashboard(), getProjects(), getTags()])
      .then(([stats, projects, tags]) => setDashboard({
        ...stats,
        projectsCount: projects.length,
        tagsCount: tags.length,
      }))
      .catch(() => setError('Не удалось загрузить данные дашборда.'))
      .finally(() => setLoading(false))
  }, [])

  const donePercent = dashboard?.totalTasks
    ? Math.round((dashboard.doneTasks / dashboard.totalTasks) * 100)
    : 0

  return (
    <main className="dashboard-page">
      <div className="dashboard-shell">
        <header className="dashboard-hero">
          <div>
            <p className="eyebrow">Обзор дня</p>
            <h1>Доброе утро, {user?.username || 'пользователь'}</h1>
            <p className="hero-subtitle">Соберём главное, чтобы день оставался под контролем.</p>
          </div>
          <time className="today-date">
            {new Intl.DateTimeFormat('ru-RU', { dateStyle: 'long' }).format(new Date())}
          </time>
        </header>

        {error && <p className="form-error">{error}</p>}
        {loading ? (
          <section className="dashboard-panel">Загружаем обзор…</section>
        ) : dashboard ? (
          <>
            <section className="dashboard-stats">
              <article className="dashboard-stat"><span className="stat-label">Всего задач</span><strong>{dashboard.totalTasks}</strong><span className="stat-hint">Общий объём</span></article>
              <article className="dashboard-stat"><span className="stat-label">К выполнению</span><strong>{dashboard.todoTasks}</strong><span className="stat-hint">Ожидают работы</span></article>
              <article className="dashboard-stat"><span className="stat-label">В работе</span><strong>{dashboard.inProgressTasks}</strong><span className="stat-hint">Активные задачи</span></article>
              <article className="dashboard-stat"><span className="stat-label">Просрочено</span><strong>{dashboard.overdueTasks}</strong><span className="stat-hint">Требуют внимания</span></article>
            </section>

            <section className="dashboard-grid">
              <article className="dashboard-panel">
                <div className="panel-heading">
                  <h2>Задачи на сегодня</h2>
                  <button className="primary-action" type="button" onClick={() => window.location.assign('/tasks')}>+ Новая задача</button>
                </div>
                <div className="task-preview-list">
                  <div className="task-preview"><span className="task-dot task-dot--high" /><span>Откройте раздел «Задачи», чтобы выбрать следующий шаг</span><span>→</span></div>
                  <div className="task-preview"><span className="task-dot" /><span>Отфильтруйте задачи по сроку или приоритету</span><span>→</span></div>
                  <div className="task-preview"><span className="task-dot task-dot--done" /><span>Выполнено задач: {dashboard.doneTasks}</span><span>✓</span></div>
                </div>
                <a href="/tasks">Перейти ко всем задачам →</a>
              </article>

              <aside className="dashboard-side">
                <article className="dashboard-panel">
                  <div className="panel-heading"><h2>Прогресс</h2><span>•••</span></div>
                  <div className="progress-ring"><strong>{donePercent}%</strong></div>
                  <div className="progress-meta"><span>Выполнено</span><strong>{dashboard.doneTasks}</strong></div>
                  <div className="progress-meta"><span>В работе</span><strong>{dashboard.inProgressTasks}</strong></div>
                </article>
                <article className="dashboard-panel">
                  <h2>Быстрый обзор</h2>
                  <div className="quick-row"><span>Проекты</span><strong>{dashboard.projectsCount}</strong></div>
                  <div className="quick-row"><span>Теги</span><strong>{dashboard.tagsCount}</strong></div>
                  <div className="quick-row"><span>Высокий приоритет</span><strong>{dashboard.highPriorityTasks}</strong></div>
                </article>
              </aside>
            </section>
          </>
        ) : null}
      </div>
    </main>
  )
}
