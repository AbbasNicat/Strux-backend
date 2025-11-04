import apiClient from '@/lib/api/client'
import { Unit, ProjectStats } from '@/lib/types'

class UnitService {
  async createUnit(data: Partial<Unit>): Promise<Unit> {
    return apiClient.post<Unit>('/api/units', data)
  }

  async getUnitById(unitId: string): Promise<Unit> {
    return apiClient.get<Unit>(`/api/units/${unitId}`)
  }

  async getProjectUnits(projectId: string): Promise<Unit[]> {
    return apiClient.get<Unit[]>(`/api/units/project/${projectId}`)
  }

  async getBuildingUnits(buildingId: string): Promise<Unit[]> {
    return apiClient.get<Unit[]>(`/api/units/building/${buildingId}`)
  }

  async getUnitsByBlock(projectId: string, blockName: string): Promise<Unit[]> {
    return apiClient.get<Unit[]>(`/api/units/project/${projectId}/block/${blockName}`)
  }

  async getUnitsByFloor(projectId: string, floor: number): Promise<Unit[]> {
    return apiClient.get<Unit[]>(`/api/units/project/${projectId}/floor/${floor}`)
  }

  async getUnitsByStatus(projectId: string, status: string): Promise<Unit[]> {
    return apiClient.get<Unit[]>(`/api/units/project/${projectId}/status/${status}`)
  }

  async getUnitsBySaleStatus(projectId: string, saleStatus: string): Promise<Unit[]> {
    return apiClient.get<Unit[]>(`/api/units/project/${projectId}/sale-status/${saleStatus}`)
  }

  async getUnitsByType(projectId: string, type: string): Promise<Unit[]> {
    return apiClient.get<Unit[]>(`/api/units/project/${projectId}/type/${type}`)
  }

  async getOwnerUnits(ownerId: string): Promise<Unit[]> {
    return apiClient.get<Unit[]>(`/api/units/owner/${ownerId}`)
  }

  async getAvailableUnits(projectId: string): Promise<Unit[]> {
    return apiClient.get<Unit[]>(`/api/units/project/${projectId}/available`)
  }

  async getOverdueUnits(projectId: string): Promise<Unit[]> {
    return apiClient.get<Unit[]>(`/api/units/project/${projectId}/overdue`)
  }

  async searchUnits(criteria: any): Promise<Unit[]> {
    return apiClient.post<Unit[]>('/api/units/search', criteria)
  }

  async updateUnit(unitId: string, data: Partial<Unit>): Promise<Unit> {
    return apiClient.put<Unit>(`/api/units/${unitId}`, data)
  }

  async updateProgress(unitId: string, progress: number): Promise<Unit> {
    return apiClient.put<Unit>(`/api/units/${unitId}/progress`, { progress })
  }

  async reserveUnit(unitId: string, data: { reservedBy: string }): Promise<Unit> {
    return apiClient.put<Unit>(`/api/units/${unitId}/reserve`, data)
  }

  async sellUnit(unitId: string, data: { ownerId: string; price: number }): Promise<Unit> {
    return apiClient.put<Unit>(`/api/units/${unitId}/sell`, data)
  }

  async cancelReservation(unitId: string): Promise<Unit> {
    return apiClient.put<Unit>(`/api/units/${unitId}/cancel-reservation`)
  }

  async deleteUnit(unitId: string, hardDelete: boolean = false): Promise<void> {
    return apiClient.delete(`/api/units/${unitId}`, {
      params: { hardDelete },
    })
  }

  async getProjectStats(projectId: string): Promise<ProjectStats> {
    return apiClient.get<ProjectStats>(`/api/units/project/${projectId}/stats`)
  }

  // Work Items
  async createWorkItem(unitId: string, data: any): Promise<any> {
    return apiClient.post(`/api/units/${unitId}/work-items`, data)
  }

  async getWorkItems(unitId: string): Promise<any[]> {
    return apiClient.get(`/api/units/${unitId}/work-items`)
  }

  async updateWorkItem(workItemId: string, data: any): Promise<any> {
    return apiClient.put(`/api/units/work-items/${workItemId}`, data)
  }

  async deleteWorkItem(workItemId: string): Promise<void> {
    return apiClient.delete(`/api/units/work-items/${workItemId}`)
  }
}

export const unitService = new UnitService()
export default unitService
