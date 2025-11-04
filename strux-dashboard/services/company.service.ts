import apiClient from '@/lib/api/client'
import { Company, PaginatedResponse, PageParams } from '@/lib/types'

class CompanyService {
  async createCompany(data: Partial<Company>): Promise<Company> {
    return apiClient.post<Company>('/api/companies', data)
  }

  async updateCompany(companyId: string, data: Partial<Company>): Promise<Company> {
    return apiClient.put<Company>(`/api/companies/${companyId}`, data)
  }

  async updateStatus(companyId: string, status: string): Promise<Company> {
    return apiClient.patch<Company>(`/api/companies/${companyId}/status`, null, {
      params: { status },
    })
  }

  async getCompanyById(companyId: string): Promise<Company> {
    return apiClient.get<Company>(`/api/companies/${companyId}`)
  }

  async getCompanyByTaxId(taxId: string): Promise<Company> {
    return apiClient.get<Company>(`/api/companies/tax-id/${taxId}`)
  }

  async getAllCompanies(params?: PageParams): Promise<PaginatedResponse<Company>> {
    return apiClient.get<PaginatedResponse<Company>>('/api/companies', { params })
  }

  async getCompaniesByStatus(status: string, params?: PageParams): Promise<PaginatedResponse<Company>> {
    return apiClient.get<PaginatedResponse<Company>>(`/api/companies/status/${status}`, { params })
  }

  async getCompaniesByType(type: string, params?: PageParams): Promise<PaginatedResponse<Company>> {
    return apiClient.get<PaginatedResponse<Company>>(`/api/companies/type/${type}`, { params })
  }

  async searchCompanies(keyword: string, params?: PageParams): Promise<PaginatedResponse<Company>> {
    return apiClient.get<PaginatedResponse<Company>>('/api/companies/search', {
      params: { keyword, ...params },
    })
  }

  async getActiveCompanies(): Promise<Company[]> {
    return apiClient.get<Company[]>('/api/companies/active')
  }

  async uploadLogo(companyId: string, file: File): Promise<Company> {
    const formData = new FormData()
    formData.append('file', file)
    return apiClient.upload<Company>(`/api/companies/${companyId}/logo`, formData)
  }

  async deleteCompany(companyId: string): Promise<void> {
    return apiClient.delete(`/api/companies/${companyId}`)
  }

  async incrementProjectCount(companyId: string, isActive: boolean): Promise<void> {
    return apiClient.patch(`/api/companies/${companyId}/projects/increment`, null, {
      params: { isActive },
    })
  }

  async healthCheck(): Promise<{ status: string }> {
    return apiClient.get('/api/companies/health')
  }
}

export const companyService = new CompanyService()
export default companyService
