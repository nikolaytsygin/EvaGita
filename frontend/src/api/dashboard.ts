import apiClient from './client'
import type { Dashboard } from '../types/dashboard'

export const getDashboard = async (): Promise<Dashboard> => {
  const response = await apiClient.get<Dashboard>('/dashboard')
  return response.data
}
