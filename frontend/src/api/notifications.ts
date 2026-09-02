import apiClient from './client'
import type { Notification } from '../types/notification'

export const getNotifications = async (): Promise<Notification[]> => {
  const response = await apiClient.get<Notification[]>('/notifications')
  return response.data
}

export const getUnreadNotifications = async (): Promise<Notification[]> => {
  const response = await apiClient.get<Notification[]>(
    '/notifications/unread',
  )
  return response.data
}

export const getUnreadNotificationsCount = async (): Promise<number> => {
  const response = await apiClient.get<number>(
    '/notifications/unread/count',
  )
  return response.data
}

export const markNotificationAsRead = async (
  id: number,
): Promise<Notification> => {
  const response = await apiClient.patch<Notification>(
    `/notifications/${id}/read`,
  )
  return response.data
}
