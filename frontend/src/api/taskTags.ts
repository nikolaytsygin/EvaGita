import apiClient from './client'
import type { Tag } from '../types/tag'

export const getTaskTags = async (taskId: number): Promise<Tag[]> => {
  const response = await apiClient.get<Tag[]>(`/tasks/${taskId}/tags`)
  return response.data
}

export const addTagToTask = async (
  taskId: number,
  tagId: number,
): Promise<void> => {
  await apiClient.post(`/tasks/${taskId}/tags/${tagId}`)
}

export const removeTagFromTask = async (
  taskId: number,
  tagId: number,
): Promise<void> => {
  await apiClient.delete(`/tasks/${taskId}/tags/${tagId}`)
}
