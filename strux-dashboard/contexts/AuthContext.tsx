'use client'

import { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import { useRouter } from 'next/navigation'
import apiClient from '@/lib/api/client'
import authService from '@/services/auth.service'
import { userService } from '@/services/user.service'
import { User } from '@/lib/types'

interface AuthContextType {
  user: User | null
  loading: boolean
  login: (email: string, password: string) => Promise<void>
  register: (data: any) => Promise<void>
  logout: () => Promise<void>
  updateUser: (data: Partial<User>) => Promise<void>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)
  const router = useRouter()

  useEffect(() => {
    // Check if user is logged in on mount
    const token = apiClient.getToken()
    if (token) {
      loadUser()
    } else {
      setLoading(false)
    }
  }, [])

  const loadUser = async () => {
    try {
      // In a real app, you would decode the JWT token to get user ID
      // For now, we'll use a mock user ID from localStorage
      const userId = localStorage.getItem('userId')
      if (userId) {
        const userData = await userService.getUserProfile(userId)
        setUser(userData)
      }
    } catch (error) {
      console.error('Failed to load user:', error)
      apiClient.setToken(null)
      localStorage.removeItem('userId')
    } finally {
      setLoading(false)
    }
  }

  const login = async (email: string, password: string) => {
    try {
      const response = await authService.login({ email, password })
      apiClient.setToken(response.token)
      localStorage.setItem('userId', response.userId)
      localStorage.setItem('refreshToken', response.refreshToken)

      await loadUser()
      router.push('/dashboard')
    } catch (error: any) {
      throw new Error(error.message || 'Login failed')
    }
  }

  const register = async (data: any) => {
    try {
      const response = await authService.register(data)
      apiClient.setToken(response.token)
      localStorage.setItem('userId', response.userId)
      localStorage.setItem('refreshToken', response.refreshToken)

      await loadUser()
      router.push('/dashboard')
    } catch (error: any) {
      throw new Error(error.message || 'Registration failed')
    }
  }

  const logout = async () => {
    try {
      await authService.logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      setUser(null)
      localStorage.removeItem('userId')
      localStorage.removeItem('refreshToken')
      router.push('/login')
    }
  }

  const updateUser = async (data: Partial<User>) => {
    if (!user) return

    try {
      const updatedUser = await userService.updateUser(user.id, data)
      setUser(updatedUser)
    } catch (error: any) {
      throw new Error(error.message || 'Failed to update user')
    }
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, updateUser }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
