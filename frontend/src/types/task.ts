export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE'

export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH'

export interface Tag {
  id: number
  name: string
}

export interface Task {
  id: number
  title: string
  description: string | null
  status: TaskStatus
  priority: TaskPriority
  dueDate: string | null
  projectId: number | null
  tags: Tag[]
  createdAt: string
  updatedAt: string
}

export interface TaskRequest {
  title: string
  description: string
  status: TaskStatus
  priority: TaskPriority
  dueDate: string | null
  projectId: number | null
}

export interface TaskFilters {
  status?: TaskStatus
  priority?: TaskPriority
  projectId?: number
  tagId?: number
  dueDateFrom?: string
  dueDateTo?: string
}

export interface TaskStatusStatistics {
  todo: number
  inProgress: number
  done: number
}
