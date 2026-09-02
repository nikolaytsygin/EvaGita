import { useEffect, useState, type FormEvent } from 'react'
import {
  createTag,
  deleteTag,
  getTags,
  updateTag,
} from '../api/tags'
import type { Tag } from '../types/tag'

export default function TagsPage() {
  const [tags, setTags] = useState<Tag[]>([])
  const [name, setName] = useState('')
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editingName, setEditingName] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const loadTags = async () => {
    try {
      setError('')
      const data = await getTags()
      setTags(data)
    } catch {
      setError('Failed to load tags')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadTags()
  }, [])

  const handleCreate = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!name.trim()) {
      setError('Tag name is required')
      return
    }

    try {
      setSaving(true)
      setError('')

      const tag = await createTag({
        name: name.trim(),
      })

      setTags((current) => [...current, tag])
      setName('')
    } catch {
      setError('Failed to create tag')
    } finally {
      setSaving(false)
    }
  }

  const startEditing = (tag: Tag) => {
    setEditingId(tag.id)
    setEditingName(tag.name)
    setError('')
  }

  const cancelEditing = () => {
    setEditingId(null)
    setEditingName('')
  }

  const handleUpdate = async (id: number) => {
    if (!editingName.trim()) {
      setError('Tag name is required')
      return
    }

    try {
      setSaving(true)
      setError('')

      const updated = await updateTag(id, {
        name: editingName.trim(),
      })

      setTags((current) =>
        current.map((tag) => (tag.id === id ? updated : tag)),
      )

      cancelEditing()
    } catch {
      setError('Failed to update tag')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (id: number) => {
    if (!window.confirm('Delete this tag?')) {
      return
    }

    try {
      setError('')
      await deleteTag(id)
      setTags((current) => current.filter((tag) => tag.id !== id))
    } catch {
      setError('Failed to delete tag')
    }
  }

  return (
    <main className="tasks-page">
      <header>
        <h1>Tags</h1>
        <p>Organize your tasks with tags.</p>
      </header>

      {error && <p className="form-error">{error}</p>}

      <section className="task-form-section">
        <h2>Create tag</h2>

        <form onSubmit={handleCreate}>
          <label>
            Tag name
            <input
              type="text"
              value={name}
              onChange={(event) => setName(event.target.value)}
              placeholder="Tag name"
              required
            />
          </label>

          <button type="submit" disabled={saving}>
            {saving ? 'Creating...' : 'Create tag'}
          </button>
        </form>
      </section>

      <section className="tasks-list">
        <div className="section-heading">
          <h2>Your tags</h2>
          <span>{tags.length}</span>
        </div>

        {loading ? (
          <p>Loading tags...</p>
        ) : tags.length === 0 ? (
          <p>No tags found.</p>
        ) : (
          <div>
            {tags.map((tag) => (
              <article className="task-card" key={tag.id}>
                {editingId === tag.id ? (
                  <>
                    <label>
                      Tag name
                      <input
                        type="text"
                        value={editingName}
                        onChange={(event) =>
                          setEditingName(event.target.value)
                        }
                      />
                    </label>

                    <div className="task-actions">
                      <button
                        type="button"
                        onClick={() => void handleUpdate(tag.id)}
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
                  </>
                ) : (
                  <>
                    <div className="task-card-header">
                      <div>
                        <h3>{tag.name}</h3>
                        <span>Tag #{tag.id}</span>
                      </div>
                    </div>

                    <div className="task-actions">
                      <button
                        type="button"
                        onClick={() => startEditing(tag)}
                      >
                        Edit
                      </button>

                      <button
                        type="button"
                        className="danger-button"
                        onClick={() => void handleDelete(tag.id)}
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
