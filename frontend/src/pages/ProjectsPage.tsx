import { useEffect, useState, type FormEvent } from 'react'
import {
  createProject,
  deleteProject,
  getProjects,
  updateProject,
} from '../api/projects'
import type { Project } from '../types/project'

const emptyForm = {
  name: '',
  description: '',
}

function ProjectsPage() {
  const [projects, setProjects] = useState<Project[]>([])
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editingName, setEditingName] = useState('')
  const [editingDescription, setEditingDescription] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const loadProjects = async () => {
    try {
      setError('')
      const data = await getProjects()
      setProjects(data)
    } catch {
      setError('Failed to load projects')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadProjects()
  }, [])

  const handleCreate = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!name.trim()) {
      setError('Project name is required')
      return
    }

    try {
      setSaving(true)
      setError('')

      const project = await createProject({
        name: name.trim(),
        description: description.trim() || undefined,
      })

      setProjects((current) => [...current, project])
      setName(emptyForm.name)
      setDescription(emptyForm.description)
    } catch {
      setError('Failed to create project')
    } finally {
      setSaving(false)
    }
  }

  const startEditing = (project: Project) => {
    setEditingId(project.id)
    setEditingName(project.name)
    setEditingDescription(project.description ?? '')
    setError('')
  }

  const cancelEditing = () => {
    setEditingId(null)
    setEditingName('')
    setEditingDescription('')
  }

  const handleUpdate = async (id: number) => {
    if (!editingName.trim()) {
      setError('Project name is required')
      return
    }

    try {
      setSaving(true)
      setError('')

      const updated = await updateProject(id, {
        name: editingName.trim(),
        description: editingDescription.trim() || undefined,
      })

      setProjects((current) =>
        current.map((project) => (project.id === id ? updated : project)),
      )

      cancelEditing()
    } catch {
      setError('Failed to update project')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (id: number) => {
    if (!window.confirm('Delete this project?')) {
      return
    }

    try {
      setError('')
      await deleteProject(id)
      setProjects((current) => current.filter((project) => project.id !== id))
    } catch {
      setError('Failed to delete project')
    }
  }

  return (
    <main className="tasks-page">
      <section className="page-header">
        <div>
          <h1>Projects</h1>
          <p>Organize your tasks by project.</p>
        </div>
      </section>

      {error && <div className="error-message">{error}</div>}

      <section className="task-form-section">
        <h2>Create project</h2>

        <form className="task-form" onSubmit={handleCreate}>
          <label>
            Name
            <input
              type="text"
              value={name}
              onChange={(event) => setName(event.target.value)}
              placeholder="Project name"
            />
          </label>

          <label>
            Description
            <textarea
              value={description}
              onChange={(event) => setDescription(event.target.value)}
              placeholder="Project description"
              rows={3}
            />
          </label>

          <button type="submit" disabled={saving}>
            {saving ? 'Creating...' : 'Create project'}
          </button>
        </form>
      </section>

      <section className="tasks-list">
        <div className="section-heading">
          <h2>Your projects</h2>
          <span>{projects.length}</span>
        </div>

        {loading ? (
          <p className="empty-state">Loading projects...</p>
        ) : projects.length === 0 ? (
          <p className="empty-state">No projects found</p>
        ) : (
          <div className="project-list">
            {projects.map((project) => (
              <article className="task-card" key={project.id}>
                {editingId === project.id ? (
                  <div className="task-edit-form">
                    <label>
                      Name
                      <input
                        type="text"
                        value={editingName}
                        onChange={(event) =>
                          setEditingName(event.target.value)
                        }
                      />
                    </label>

                    <label>
                      Description
                      <textarea
                        value={editingDescription}
                        onChange={(event) =>
                          setEditingDescription(event.target.value)
                        }
                        rows={3}
                      />
                    </label>

                    <div className="task-actions">
                      <button
                        type="button"
                        onClick={() => void handleUpdate(project.id)}
                        disabled={saving}
                      >
                        Save
                      </button>

                      <button
                        type="button"
                        className="secondary-button"
                        onClick={cancelEditing}
                        disabled={saving}
                      >
                        Cancel
                      </button>
                    </div>
                  </div>
                ) : (
                  <>
                    <div className="task-card-header">
                      <div>
                        <h3>{project.name}</h3>
                        <span>Project #{project.id}</span>
                      </div>
                    </div>

                    {project.description && (
                      <p className="task-description">{project.description}</p>
                    )}

                    <div className="task-actions">
                      <button
                        type="button"
                        onClick={() => startEditing(project)}
                      >
                        Edit
                      </button>

                      <button
                        type="button"
                        className="danger-button"
                        onClick={() => void handleDelete(project.id)}
                      >
                        Delete
                      </button>
                    </div>
                  </>
                )}
              </article>
            ))}
          </div>
        )}
      </section>
    </main>
  )
}

export default ProjectsPage
