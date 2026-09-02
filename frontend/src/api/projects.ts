import apiClient from './client'
import type { Project } from '../types/project'

export interface CreateProjectRequest {
  name: string
  description?: string
}

export interface UpdateProjectRequest {
  name: string
  description?: string
}

export const getProjects = async (): Promise<Project[]> => {
  const response = await apiClient.get<Project[]>('/projects')
  return response.data
}

export const createProject = async (
  data: CreateProjectRequest,
): Promise<Project> => {
  const response = await apiClient.post<Project>('/projects', data)
  return response.data
}

export const updateProject = async (
  id: number,
  data: UpdateProjectRequest,
): Promise<Project> => {
  const response = await apiClient.put<Project>(`/projects/${id}`, data)
  return response.data
}

export const deleteProject = async (id: number): Promise<void> => {
  await apiClient.delete(`/projects/${id}`)
}
