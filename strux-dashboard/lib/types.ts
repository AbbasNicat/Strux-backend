// Auth Types
export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  firstName: string
  lastName: string
  email: string
  password: string
  phoneNumber: string
}

export interface AuthResponse {
  token: string
  refreshToken: string
  userId: string
  email: string
}

// User Types
export interface User {
  id: string
  keycloakId: string
  email: string
  firstName: string
  lastName: string
  phoneNumber?: string
  profileImageUrl?: string
  city?: string
  specialty?: string
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED'
  createdAt: string
  updatedAt: string
}

export interface WorkerProfile {
  id: string
  userId: string
  specialty: string
  city: string
  isAvailable: boolean
  availableFrom?: string
  rating: number
  completedProjects: number
  performanceScore: number
  yearsOfExperience?: number
}

export interface Worker extends User {
  workerProfile: WorkerProfile
}

// Company Types
export interface Company {
  id: string
  name: string
  taxId: string
  type: 'CONSTRUCTION' | 'DEVELOPER' | 'CONTRACTOR'
  status: 'ACTIVE' | 'INACTIVE' | 'PENDING'
  phoneNumber?: string
  email?: string
  address?: string
  city?: string
  logoUrl?: string
  activeProjectCount: number
  totalProjectCount: number
  createdAt: string
}

// Project Types
export interface Project {
  id: string
  companyId: string
  name: string
  description?: string
  status: 'PLANNING' | 'IN_PROGRESS' | 'COMPLETED' | 'ON_HOLD' | 'CANCELLED'
  progress: number
  startDate?: string
  endDate?: string
  estimatedCompletionDate?: string
  city?: string
  address?: string
  latitude?: number
  longitude?: number
  createdAt: string
  updatedAt: string
}

export interface ProjectPhase {
  id: string
  projectId: string
  name: string
  description?: string
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED'
  progress: number
  startDate?: string
  endDate?: string
  order: number
}

// Task Types
export interface Task {
  id: string
  companyId: string
  projectId?: string
  title: string
  description?: string
  type: 'CONSTRUCTION' | 'INSPECTION' | 'MAINTENANCE' | 'REPAIR' | 'OTHER'
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
  assignedTo?: string
  createdBy: string
  dueDate?: string
  completedAt?: string
  progress: number
  createdAt: string
  updatedAt: string
}

// Unit Types
export interface Unit {
  id: string
  projectId: string
  buildingId?: string
  unitNumber: string
  blockName?: string
  floor: number
  type: 'STUDIO' | 'ONE_BEDROOM' | 'TWO_BEDROOM' | 'THREE_BEDROOM' | 'PENTHOUSE'
  area?: number
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'DELAYED'
  saleStatus: 'AVAILABLE' | 'RESERVED' | 'SOLD'
  progress: number
  price?: number
  ownerId?: string
  reservedBy?: string
  reservedAt?: string
  soldAt?: string
  estimatedCompletionDate?: string
  createdAt: string
  updatedAt: string
}

// Notification Types
export interface Notification {
  id: string
  userId: string
  type: 'INFO' | 'WARNING' | 'ERROR' | 'SUCCESS'
  title: string
  message: string
  isRead: boolean
  createdAt: string
}

export interface NotificationPreferences {
  userId: string
  emailEnabled: boolean
  smsEnabled: boolean
  pushEnabled: boolean
}

// Stats Types
export interface DashboardStats {
  activeProjects: number
  totalTasks: number
  totalWorkers: number
  completedUnits: number
}

export interface ProjectStats {
  totalUnits: number
  completedUnits: number
  inProgressUnits: number
  notStartedUnits: number
  availableUnits: number
  reservedUnits: number
  soldUnits: number
}

// Pagination Types
export interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface PageParams {
  page?: number
  size?: number
  sortBy?: string
  direction?: 'ASC' | 'DESC'
}
