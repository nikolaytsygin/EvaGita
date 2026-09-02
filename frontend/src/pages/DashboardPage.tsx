import { useEffect, useState } from 'react'
import { getDashboard } from '../api/dashboard'
import { getProjects } from '../api/projects'
import { getTags } from '../api/tags'
import { useAuth } from '../hooks/useAuth'
import type { Dashboard } from '../types/dashboard'

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
    const loadDashboard = async () => {
      try {
        setLoading(true)
        setError('')

        const [taskStats, projects, tags] = await Promise.all([
          getDashboard(),
          getProjects(),
          getTags(),
        ])

        setDashboard({
          ...taskStats,
          projectsCount: projects.length,
          tagsCount: tags.length,
        })
      } catch {
        setError('Failed to load dashboard.')
      } finally {
        setLoading(false)
      }
    }

    void loadDashboard()
  }, [])

  return (
    <main className="dashboard-page">


      <section className="dashboard-content">
        <header>
          <h1>EvaGita Dashboard</h1>
          <p>Welcome, {user?.username}!</p>
        </header>

        {error && <p className="form-error">{error}</p>}

        {loading ? (
          <p>Loading dashboard...</p>
        ) : dashboard ? (
          <>
            <section className="dashboard-stats">
              <article className="dashboard-card">
                <span>Total tasks</span>
                <strong>{dashboard.totalTasks}</strong>
              </article>

              <article className="dashboard-card">
                <span>TODO</span>
                <strong>{dashboard.todoTasks}</strong>
              </article>

              <article className="dashboard-card">
                <span>In progress</span>
                <strong>{dashboard.inProgressTasks}</strong>
              </article>

              <article className="dashboard-card">
                <span>Done</span>
                <strong>{dashboard.doneTasks}</strong>
              </article>

              <article className="dashboard-card">
                <span>Overdue</span>
                <strong>{dashboard.overdueTasks}</strong>
              </article>

              <article className="dashboard-card">
                <span>Projects</span>
                <strong>{dashboard.projectsCount}</strong>
              </article>

              <article className="dashboard-card">
                <span>Tags</span>
                <strong>{dashboard.tagsCount}</strong>
              </article>
            </section>

            <section className="dashboard-section">
              <h2>Tasks by priority</h2>

              <div className="priority-stats">
                <div>
                  <span>Low</span>
                  <strong>{dashboard.lowPriorityTasks}</strong>
                </div>

                <div>
                  <span>Medium</span>
                  <strong>{dashboard.mediumPriorityTasks}</strong>
                </div>

                <div>
                  <span>High</span>
                  <strong>{dashboard.highPriorityTasks}</strong>
                </div>
              </div>
            </section>
          </>
        ) : null}
      </section>
    </main>
  )
}
