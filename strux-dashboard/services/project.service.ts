import apiClient from '@/lib/api/client'
import { Project, ProjectPhase, PaginatedResponse, PageParams } from '@/lib/types'

class ProjectService {
  async createProject(data: Partial<Project>): Promise<Project> {
    return apiClient.post<Project>('/api/projects', data)
  }

  async getAllProjects(params?: PageParams): Promise<PaginatedResponse<Project>> {
    return apiClient.get<PaginatedResponse<Project>>('/api/projects', { params })
  }

  async getProjectById(projectId: string): Promise<Project> {
    return apiClient.get<Project>(`/api/projects/${projectId}`)
  }

  async updateProject(projectId: string, data: Partial<Project>): Promise<Project> {
    return apiClient.put<Project>(`/api/projects/${projectId}`, data)
  }

  async patchProject(projectId: string, data: Partial<Project>): Promise<Project> {
    return apiClient.patch<Project>(`/api/projects/${projectId}`, data)
  }

  async deleteProject(projectId: string): Promise<void> {
    return apiClient.delete(`/api/projects/${projectId}`)
  }

  async getProjectProgress(projectId: string): Promise<{ progress: number }> {
    return apiClient.get(`/api/projects/${projectId}/progress`)
  }

  async getProjectsForMap(): Promise<Project[]> {
    return apiClient.get<Project[]>('/api/projects/map')
  }

  async getCompanyProjects(companyId: string): Promise<Project[]> {
    return apiClient.get<Project[]>(`/api/projects/company/${companyId}`)
  }

  // Phases
  async createPhase(projectId: string, data: Partial<ProjectPhase>): Promise<ProjectPhase> {
    return apiClient.post<ProjectPhase>(`/api/projects/${projectId}/phases`, data)
  }

  async updatePhaseProgress(
    projectId: string,
    phaseId: string,
    progress: number
  ): Promise<ProjectPhase> {
    return apiClient.patch<ProjectPhase>(
      `/api/projects/${projectId}/phases/${phaseId}/progress`,
      { progress }
    )
  }

  async deletePhase(projectId: string, phaseId: string): Promise<void> {
    return apiClient.delete(`/api/projects/${projectId}/phases/${phaseId}`)
  }
}

class LocationService {
  async searchLocations(query: string): Promise<any[]> {
    return apiClient.get('/api/locations/search', { params: { query } })
  }

  async getLocationDetails(placeId: string): Promise<any> {
    return apiClient.get(`/api/locations/details/${placeId}`)
  }

  async getProjectLocation(projectId: string): Promise<any> {
    return apiClient.get(`/api/locations/projects/${projectId}`)
  }

  async getMapMarkers(bounds: {
    southWestLat: number
    southWestLng: number
    northEastLat: number
    northEastLng: number
  }): Promise<any[]> {
    return apiClient.get('/api/locations/markers', { params: bounds })
  }

  async getProjectMapDetails(projectId: string): Promise<any> {
    return apiClient.get(`/api/locations/projects/${projectId}/map-details`)
  }

  async getNearbyProjects(latitude: number, longitude: number, radiusKm: number = 10): Promise<any[]> {
    return apiClient.get('/api/locations/nearby', {
      params: { latitude, longitude, radiusKm },
    })
  }

  async filterProjects(filters: any): Promise<any[]> {
    return apiClient.post('/api/locations/filter', filters)
  }
}

export const projectService = new ProjectService()
export const locationService = new LocationService()
