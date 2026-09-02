import { useEffect, useState } from 'react'
import { getProjects } from '../api/projects'
import { getTags } from '../api/tags'
import {
  addTagToTask,
  getTaskTags,
  removeTagFromTask,
} from '../api/taskTags'
import type { FormEvent } from 'react'
import {
  createTask,
  deleteTask,
  getTasks,
  updateTask,
} from '../api/tasks'
import type {
  Task,
  TaskPriority,
  TaskRequest,
  TaskStatus,
} from '../types/task'
import type { Project } from '../types/project'
import type { Tag } from '../types/tag'

const emptyForm: TaskRequest = {
  title: '',
  description: '',
  status: 'TODO',
  priority: 'MEDIUM',
  dueDate: null,
  projectId: null,
}

export default function TasksPage() {
  const [tasks, setTasks] = useState<Task[]>([])
  const [projects, setProjects] = useState<Project[]>([])
  const [tags, setTags] = useState<Tag[]>([])
  const [selectedTagIds, setSelectedTagIds] = useState<number[]>([])
  const [form, setForm] = useState<TaskRequest>(emptyForm)
  const [editingId, setEditingId] = useState<number | null>(null)

  const [search, setSearch] = useState('')
  const [status, setStatus] = useState<TaskStatus | ''>('')
  const [priority, setPriority] = useState<TaskPriority | ''>('')
  const [projectId, setProjectId] = useState('')
  const [tagId, setTagId] = useState('')

  const [loading, setLoading] = useState(true)
  const [projectsLoading, setProjectsLoading] = useState(true)
  const [tagsLoading, setTagsLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const loadTasks = async () => {
    setLoading(true)
    setError('')

    try {
      const filters = {
        ...(status ? { status } : {}),
        ...(priority ? { priority } : {}),
        ...(projectId ? { projectId: Number(projectId) } : {}),
        ...(tagId ? { tagId: Number(tagId) } : {}),
      }

      let data = await getTasks(filters)

      if (search.trim()) {
        const searchText = search.trim().toLowerCase()

        data = data.filter((task) =>
          task.title.toLowerCase().includes(searchText)
        )
      }

      setTasks(data)
    } catch {
      setError('Failed to load tasks.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    const loadProjects = async () => {
      try {
        setProjectsLoading(true)
        const data = await getProjects()
        setProjects(data)
      } catch {
        setError('Failed to load projects.')
      } finally {
        setProjectsLoading(false)
      }
    }

    const loadTags = async () => {
      try {
        setTagsLoading(true)
        const data = await getTags()
        setTags(data)
      } catch {
        setError('Failed to load tags.')
      } finally {
        setTagsLoading(false)
      }
    }

    void loadProjects()
    void loadTags()
  }, [])

  useEffect(() => {
    void loadTasks()
  }, [status, priority, projectId, tagId])

  const handleSearch = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    await loadTasks()
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!form.title.trim()) {
      setError('Task title is required.')
      return
    }

    setSaving(true)
    setError('')

    try {
      const request: TaskRequest = {
        ...form,
        title: form.title.trim(),
        description: form.description.trim(),
        dueDate: form.dueDate || null,
        projectId: form.projectId || null,
      }

      let taskId = editingId

      if (editingId === null) {
        const createdTask = await createTask(request)
        taskId = createdTask.id
      } else {
        await updateTask(editingId, request)
      }

      if (taskId !== null) {
        const currentTags = await getTaskTags(taskId)
        const currentTagIds = currentTags.map((tag) => tag.id)

        const tagsToAdd = selectedTagIds.filter(
          (tagId) => !currentTagIds.includes(tagId),
        )

        const tagsToRemove = currentTagIds.filter(
          (tagId) => !selectedTagIds.includes(tagId),
        )

        await Promise.all([
          ...tagsToAdd.map((tagId) =>
            addTagToTask(taskId!, tagId),
          ),
          ...tagsToRemove.map((tagId) =>
            removeTagFromTask(taskId!, tagId),
          ),
        ])
      }

      setForm(emptyForm)
      setSelectedTagIds([])
      setEditingId(null)
      await loadTasks()
    } catch {
      setError('Failed to save task.')
    } finally {
      setSaving(false)
    }
  }

  const handleEdit = (task: Task) => {
    setEditingId(task.id)

    setForm({
      title: task.title,
      description: task.description ?? '',
      status: task.status,
      priority: task.priority,
      dueDate: task.dueDate,
      projectId: task.projectId,
    })

    setSelectedTagIds(task.tags.map((tag) => tag.id))

    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    })
  }

  const handleDelete = async (id: number) => {
    if (!window.confirm('Delete this task?')) {
      return
    }

    try {
      await deleteTask(id)

      if (editingId === id) {
        setEditingId(null)
        setForm(emptyForm)
      }

      await loadTasks()
    } catch {
      setError('Failed to delete task.')
    }
  }

  const handleReset = () => {
    setForm(emptyForm)
    setSelectedTagIds([])
    setEditingId(null)
    setError('')
  }

  const clearFilters = () => {
    setSearch('')
    setStatus('')
    setPriority('')
    setProjectId('')
    setTagId('')
  }

  return (
    <main className="tasks-page">
      <header>
        <h1>Tasks</h1>
        <p>Manage your EvaGita tasks.</p>
      </header>

      <section className="task-form-section">
        <h2>
          {editingId === null ? 'Create task' : 'Edit task'}
        </h2>

        <form onSubmit={handleSubmit}>
          <label>
            Title
            <input
              type="text"
              value={form.title}
              onChange={(event) =>
                setForm({
                  ...form,
                  title: event.target.value,
                })
              }
              required
            />
          </label>

          <label>
            Description
            <textarea
              value={form.description}
              onChange={(event) =>
                setForm({
                  ...form,
                  description: event.target.value,
                })
              }
              rows={4}
            />
          </label>

          <label>
            Status
            <select
              value={form.status}
              onChange={(event) =>
                setForm({
                  ...form,
                  status: event.target.value as TaskStatus,
                })
              }
            >
              <option value="TODO">TODO</option>
              <option value="IN_PROGRESS">IN PROGRESS</option>
              <option value="DONE">DONE</option>
            </select>
          </label>

          <label>
            Priority
            <select
              value={form.priority}
              onChange={(event) =>
                setForm({
                  ...form,
                  priority: event.target.value as TaskPriority,
                })
              }
            >
              <option value="LOW">LOW</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="HIGH">HIGH</option>
            </select>
          </label>

          <label>
            Due date
            <input
              type="date"
              value={form.dueDate ?? ''}
              onChange={(event) =>
                setForm({
                  ...form,
                  dueDate: event.target.value || null,
                })
              }
            />
          </label>

          <label>
            Project
            <select
              value={form.projectId ?? ''}
              onChange={(event) =>
                setForm({
                  ...form,
                  projectId: event.target.value
                    ? Number(event.target.value)
                    : null,
                })
              }
              disabled={projectsLoading}
            >
              <option value="">
                {projectsLoading
                  ? 'Loading projects...'
                  : 'No project'}
              </option>

              {projects.map((project) => (
                <option key={project.id} value={project.id}>
                  {project.name}
                </option>
              ))}
            </select>
          </label>

          <fieldset>
            <legend>Tags</legend>

            {tagsLoading ? (
              <p>Loading tags...</p>
            ) : tags.length === 0 ? (
              <p>No tags available.</p>
            ) : (
              <div className="tag-checkboxes">
                {tags.map((tag) => (
                  <label key={tag.id} className="tag-checkbox">
                    <input
                      type="checkbox"
                      checked={selectedTagIds.includes(tag.id)}
                      onChange={(event) => {
                        setSelectedTagIds((current) =>
                          event.target.checked
                            ? [...current, tag.id]
                            : current.filter(
                                (id) => id !== tag.id,
                              ),
                        )
                      }}
                    />
                    <span>{tag.name}</span>
                  </label>
                ))}
              </div>
            )}
          </fieldset>

          <div>
            <button type="submit" disabled={saving}>
              {saving
                ? 'Saving...'
                : editingId === null
                  ? 'Create task'
                  : 'Update task'}
            </button>

            {editingId !== null && (
              <button
                type="button"
                onClick={handleReset}
              >
                Cancel
              </button>
            )}
          </div>
        </form>
      </section>

      <section className="task-filters">
        <h2>Search and filters</h2>

        <form onSubmit={handleSearch}>
          <label>
            Search title
            <input
              type="search"
              value={search}
              onChange={(event) =>
                setSearch(event.target.value)
              }
              placeholder="Search by title..."
            />
          </label>

          <label>
            Status
            <select
              value={status}
              onChange={(event) =>
                setStatus(
                  event.target.value as TaskStatus | ''
                )
              }
            >
              <option value="">All statuses</option>
              <option value="TODO">TODO</option>
              <option value="IN_PROGRESS">IN PROGRESS</option>
              <option value="DONE">DONE</option>
            </select>
          </label>

          <label>
            Priority
            <select
              value={priority}
              onChange={(event) =>
                setPriority(
                  event.target.value as TaskPriority | ''
                )
              }
            >
              <option value="">All priorities</option>
              <option value="LOW">LOW</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="HIGH">HIGH</option>
            </select>
          </label>

          <label>
            Project
            <select
              value={projectId}
              onChange={(event) =>
                setProjectId(event.target.value)
              }
              disabled={projectsLoading}
            >
              <option value="">
                {projectsLoading
                  ? 'Loading projects...'
                  : 'All projects'}
              </option>

              {projects.map((project) => (
                <option key={project.id} value={project.id}>
                  {project.name}
                </option>
              ))}
            </select>
          </label>

          <label>
            Tag
            <select
              value={tagId}
              onChange={(event) =>
                setTagId(event.target.value)
              }
              disabled={tagsLoading}
            >
              <option value="">
                {tagsLoading ? 'Loading tags...' : 'All tags'}
              </option>

              {tags.map((tag) => (
                <option key={tag.id} value={tag.id}>
                  {tag.name}
                </option>
              ))}
            </select>
          </label>

          <div>
            <button type="submit">Search</button>

            <button
              type="button"
              onClick={clearFilters}
            >
              Clear filters
            </button>
          </div>
        </form>
      </section>

      {error && (
        <p className="form-error">
          {error}
        </p>
      )}

      <section className="tasks-list">
        <h2>Your tasks</h2>

        {loading ? (
          <p>Loading tasks...</p>
        ) : tasks.length === 0 ? (
          <p>No tasks found.</p>
        ) : (
          <div>
            {tasks.map((task) => (
              <article key={task.id}>
                <h3>{task.title}</h3>

                {task.description && (
                  <p>{task.description}</p>
                )}

                <p>
                  Status: <strong>{task.status}</strong>
                </p>

                <p>
                  Priority: <strong>{task.priority}</strong>
                </p>

                {task.dueDate && (
                  <p>Due: {task.dueDate}</p>
                )}

                {task.projectId !== null && (
                  <p>
                    Project:{' '}
                    {projects.find(
                      (project) => project.id === task.projectId
                    )?.name ?? `#${task.projectId}`}
                  </p>
                )}

                {task.tags?.length > 0 && (
                  <p>
                    Tags:{' '}
                    {task.tags
                      .map((tag) => tag.name)
                      .join(', ')}
                  </p>
                )}

                <button
                  type="button"
                  onClick={() => handleEdit(task)}
                >
                  Edit
                </button>

                <button
                  type="button"
                  onClick={() => handleDelete(task.id)}
                >
                  Delete
                </button>
              </article>
            ))}
          </div>
        )}
      </section>
    </main>
  )
}
