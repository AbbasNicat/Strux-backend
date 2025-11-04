import apiClient from '@/lib/api/client'
import { Task, PaginatedResponse } from '@/lib/types'

class TaskService {
  async createTask(data: Partial<Task>): Promise<Task> {
    return apiClient.post<Task>('/api/tasks', data)
  }

  async getTaskById(taskId: string): Promise<Task> {
    return apiClient.get<Task>(`/api/tasks/${taskId}`)
  }

  async getCompanyTasks(companyId: string): Promise<Task[]> {
    return apiClient.get<Task[]>(`/api/tasks/company/${companyId}`)
  }

  async getProjectTasks(projectId: string): Promise<Task[]> {
    return apiClient.get<Task[]>(`/api/tasks/project/${projectId}`)
  }

  async getTasksByCreator(userId: string): Promise<Task[]> {
    return apiClient.get<Task[]>(`/api/tasks/creator/${userId}`)
  }

  async getAssignedTasks(userId: string): Promise<Task[]> {
    return apiClient.get<Task[]>(`/api/tasks/assigned/${userId}`)
  }

  async getTasksByStatus(companyId: string, status: string): Promise<Task[]> {
    return apiClient.get<Task[]>(`/api/tasks/company/${companyId}/status/${status}`)
  }

  async getTasksByPriority(companyId: string, priority: string): Promise<Task[]> {
    return apiClient.get<Task[]>(`/api/tasks/company/${companyId}/priority/${priority}`)
  }

  async getTasksByType(companyId: string, type: string): Promise<Task[]> {
    return apiClient.get<Task[]>(`/api/tasks/company/${companyId}/type/${type}`)
  }

  async getSubtasks(taskId: string): Promise<Task[]> {
    return apiClient.get<Task[]>(`/api/tasks/${taskId}/subtasks`)
  }

  async getOverdueTasks(companyId: string): Promise<Task[]> {
    return apiClient.get<Task[]>(`/api/tasks/company/${companyId}/overdue`)
  }

  async getRecurringTasks(companyId: string): Promise<Task[]> {
    return apiClient.get<Task[]>(`/api/tasks/company/${companyId}/recurring`)
  }

  async getTaskTemplates(companyId: string): Promise<Task[]> {
    return apiClient.get<Task[]>(`/api/tasks/company/${companyId}/templates`)
  }

  async searchTasks(criteria: any): Promise<Task[]> {
    return apiClient.post<Task[]>('/api/tasks/search', criteria)
  }

  async updateTask(taskId: string, data: Partial<Task>): Promise<Task> {
    return apiClient.put<Task>(`/api/tasks/${taskId}`, data)
  }

  async assignTask(taskId: string, userId: string): Promise<Task> {
    return apiClient.put<Task>(`/api/tasks/${taskId}/assign`, { userId })
  }

  async updateProgress(taskId: string, progress: number): Promise<Task> {
    return apiClient.put<Task>(`/api/tasks/${taskId}/progress`, { progress })
  }

  async completeTask(taskId: string): Promise<Task> {
    return apiClient.put<Task>(`/api/tasks/${taskId}/complete`)
  }

  async deleteTask(taskId: string, hardDelete: boolean = false): Promise<void> {
    return apiClient.delete(`/api/tasks/${taskId}`, {
      params: { hardDelete },
    })
  }

  async getTaskStats(companyId: string): Promise<any> {
    return apiClient.get(`/api/tasks/company/${companyId}/stats`)
  }
}

export const taskService = new TaskService()
export default taskService
