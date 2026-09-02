import client from './client'
import type {
  Task,
  TaskFilters,
  TaskRequest,
  TaskStatusStatistics,
} from '../types/task'

export const getTasks = async (
  filters?: TaskFilters
): Promise<Task[]> => {
  const response = await client.get<Task[]>('/tasks', {
    params: filters,
  })

  return response.data
}

export const searchTasks = async (
  title: string
): Promise<Task[]> => {
  const response = await client.get<Task[]>('/tasks/search', {
    params: { title },
  })

  return response.data
}

export const getTaskById = async (
  id: number
): Promise<Task> => {
  const response = await client.get<Task>(`/tasks/${id}`)

  return response.data
}

export const createTask = async (
  request: TaskRequest
): Promise<Task> => {
  const response = await client.post<Task>('/tasks', request)

  return response.data
}

export const updateTask = async (
  id: number,
  request: TaskRequest
): Promise<Task> => {
  const response = await client.put<Task>(
    `/tasks/${id}`,
    request
  )

  return response.data
}

export const deleteTask = async (
  id: number
): Promise<void> => {
  await client.delete(`/tasks/${id}`)
}

export const getTaskStatusStatistics =
  async (): Promise<TaskStatusStatistics> => {
    const response = await client.get<TaskStatusStatistics>(
      '/tasks/statistics/status'
    )

    return response.data
  }

export const addTagToTask = async (
  taskId: number,
  tagId: number
): Promise<void> => {
  await client.post(`/tasks/${taskId}/tags/${tagId}`)
}

export const removeTagFromTask = async (
  taskId: number,
  tagId: number
): Promise<void> => {
  await client.delete(`/tasks/${taskId}/tags/${tagId}`)
}
