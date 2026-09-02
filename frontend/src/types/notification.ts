export type NotificationType =
  | 'TASK_CREATED'
  | 'TASK_UPDATED'
  | 'TASK_COMPLETED'
  | 'TASK_OVERDUE'

export interface Notification {
  id: number
  type: NotificationType
  message: string
  read: boolean
  createdAt: string
  updatedAt: string
}
