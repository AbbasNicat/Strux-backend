import apiClient from '@/lib/api/client'
import { User, Worker, WorkerProfile } from '@/lib/types'

class UserService {
  async getUserById(userId: string): Promise<User> {
    return apiClient.get<User>(`/api/users/${userId}`)
  }

  async getUserProfile(userId: string): Promise<User> {
    return apiClient.get<User>(`/api/users/${userId}/profile`)
  }

  async updateUser(userId: string, data: Partial<User>): Promise<User> {
    return apiClient.put<User>(`/api/users/${userId}`, data)
  }

  async uploadAvatar(userId: string, file: File): Promise<User> {
    const formData = new FormData()
    formData.append('file', file)
    return apiClient.upload<User>(`/api/users/${userId}/avatar`, formData)
  }

  async createWorkerProfile(userId: string, data: Partial<WorkerProfile>): Promise<WorkerProfile> {
    return apiClient.post<WorkerProfile>(`/api/users/${userId}/worker-profile`, data)
  }

  async searchUsers(keyword?: string, status?: string): Promise<User[]> {
    return apiClient.get<User[]>('/api/users/search', {
      params: { keyword, status },
    })
  }

  async deleteUser(userId: string): Promise<void> {
    return apiClient.delete(`/api/users/${userId}`)
  }
}

class WorkerService {
  async searchWorkers(params: {
    specialty?: string
    city?: string
    isAvailable?: boolean
    minRating?: number
  }): Promise<Worker[]> {
    return apiClient.get<Worker[]>('/api/workers/search', { params })
  }

  async getCompanyWorkers(companyId: string): Promise<Worker[]> {
    return apiClient.get<Worker[]>(`/api/workers/company/${companyId}`)
  }

  async updateAvailability(
    workerId: string,
    isAvailable: boolean,
    availableFrom?: string
  ): Promise<Worker> {
    return apiClient.patch<Worker>(`/api/workers/${workerId}/availability`, null, {
      params: { isAvailable, availableFrom },
    })
  }

  async addPerformance(workerId: string, data: any): Promise<void> {
    return apiClient.post(`/api/workers/${workerId}/performance`, data)
  }

  async assignToProject(workerId: string, projectId: string): Promise<void> {
    return apiClient.post(`/api/workers/${workerId}/projects/${projectId}`)
  }

  async removeFromProject(workerId: string, projectId: string): Promise<void> {
    return apiClient.delete(`/api/workers/${workerId}/projects/${projectId}`)
  }

  async getTopWorkers(specialty?: string, limit: number = 10): Promise<Worker[]> {
    return apiClient.get<Worker[]>('/api/workers/top', {
      params: { specialty, limit },
    })
  }

  async getWorkerStats(workerId: string): Promise<any> {
    return apiClient.get(`/api/workers/${workerId}/stats`)
  }

  async getAvailableWorkers(city?: string, specialty?: string): Promise<Worker[]> {
    return apiClient.get<Worker[]>('/api/workers/available', {
      params: { city, specialty },
    })
  }
}

export const userService = new UserService()
export const workerService = new WorkerService()
