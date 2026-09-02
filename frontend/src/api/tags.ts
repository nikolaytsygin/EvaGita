import apiClient from './client'
import type { Tag } from '../types/tag'

export interface CreateTagRequest {
  name: string
}

export interface UpdateTagRequest {
  name: string
}

export const getTags = async (): Promise<Tag[]> => {
  const response = await apiClient.get<Tag[]>('/tags')
  return response.data
}

export const createTag = async (
  data: CreateTagRequest,
): Promise<Tag> => {
  const response = await apiClient.post<Tag>('/tags', data)
  return response.data
}

export const updateTag = async (
  id: number,
  data: UpdateTagRequest,
): Promise<Tag> => {
  const response = await apiClient.put<Tag>(`/tags/${id}`, data)
  return response.data
}

export const deleteTag = async (id: number): Promise<void> => {
  await apiClient.delete(`/tags/${id}`)
}
