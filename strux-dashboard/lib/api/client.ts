const API_GATEWAY = process.env.NEXT_PUBLIC_API_GATEWAY || 'http://localhost:8081'

interface RequestOptions extends RequestInit {
  params?: Record<string, string | number | boolean | undefined>
}

class ApiClient {
  private baseURL: string
  private token: string | null = null

  constructor(baseURL: string = API_GATEWAY) {
    this.baseURL = baseURL

    // Load token from localStorage if available
    if (typeof window !== 'undefined') {
      this.token = localStorage.getItem('token')
    }
  }

  setToken(token: string | null) {
    this.token = token
    if (typeof window !== 'undefined') {
      if (token) {
        localStorage.setItem('token', token)
      } else {
        localStorage.removeItem('token')
      }
    }
  }

  getToken(): string | null {
    return this.token
  }

  private buildURL(path: string, params?: Record<string, string | number | boolean | undefined>): string {
    const url = new URL(path, this.baseURL)

    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          url.searchParams.append(key, String(value))
        }
      })
    }

    return url.toString()
  }

  private async handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'An error occurred' }))
      throw new Error(error.message || `HTTP ${response.status}`)
    }

    const contentType = response.headers.get('content-type')
    if (contentType && contentType.includes('application/json')) {
      return response.json()
    }

    return response.text() as any
  }

  async request<T>(path: string, options: RequestOptions = {}): Promise<T> {
    const { params, headers = {}, ...fetchOptions } = options

    const url = this.buildURL(path, params)

    const defaultHeaders: Record<string, string> = {
      'Content-Type': 'application/json',
    }

    if (this.token) {
      defaultHeaders['Authorization'] = `Bearer ${this.token}`
    }

    const response = await fetch(url, {
      ...fetchOptions,
      headers: {
        ...defaultHeaders,
        ...headers,
      },
    })

    return this.handleResponse<T>(response)
  }

  async get<T>(path: string, options?: RequestOptions): Promise<T> {
    return this.request<T>(path, { ...options, method: 'GET' })
  }

  async post<T>(path: string, body?: any, options?: RequestOptions): Promise<T> {
    return this.request<T>(path, {
      ...options,
      method: 'POST',
      body: body ? JSON.stringify(body) : undefined,
    })
  }

  async put<T>(path: string, body?: any, options?: RequestOptions): Promise<T> {
    return this.request<T>(path, {
      ...options,
      method: 'PUT',
      body: body ? JSON.stringify(body) : undefined,
    })
  }

  async patch<T>(path: string, body?: any, options?: RequestOptions): Promise<T> {
    return this.request<T>(path, {
      ...options,
      method: 'PATCH',
      body: body ? JSON.stringify(body) : undefined,
    })
  }

  async delete<T>(path: string, options?: RequestOptions): Promise<T> {
    return this.request<T>(path, { ...options, method: 'DELETE' })
  }

  async upload<T>(path: string, formData: FormData, options?: RequestOptions): Promise<T> {
    const url = this.buildURL(path, options?.params)

    const headers: Record<string, string> = {}
    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`
    }

    const response = await fetch(url, {
      method: 'POST',
      headers,
      body: formData,
    })

    return this.handleResponse<T>(response)
  }
}

export const apiClient = new ApiClient()
export default apiClient
