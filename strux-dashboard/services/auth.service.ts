import apiClient from '@/lib/api/client'
import { AuthResponse, LoginRequest, RegisterRequest } from '@/lib/types'

class AuthService {
  async login(data: LoginRequest): Promise<AuthResponse> {
    return apiClient.post<AuthResponse>('/api/auth/login', data)
  }

  async register(data: RegisterRequest): Promise<AuthResponse> {
    return apiClient.post<AuthResponse>('/api/auth/register', data)
  }

  async logout(): Promise<void> {
    await apiClient.post('/api/auth/logout')
    apiClient.setToken(null)
  }

  async refreshToken(refreshToken: string): Promise<AuthResponse> {
    return apiClient.post<AuthResponse>('/api/auth/refresh', { refreshToken })
  }

  async verify2FA(code: string, sessionId: string): Promise<AuthResponse> {
    return apiClient.post<AuthResponse>('/api/auth/verify-2fa', { code, sessionId })
  }

  async googleLogin(credential: string): Promise<AuthResponse> {
    return apiClient.post<AuthResponse>('/api/auth/google', { credential })
  }

  isAuthenticated(): boolean {
    return !!apiClient.getToken()
  }
}

export const authService = new AuthService()
export default authService
