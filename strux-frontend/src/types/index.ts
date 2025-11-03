// User Types
export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
  role: 'USER' | 'WORKER' | 'COMPANY_ADMIN' | 'SUPER_ADMIN';
  companyId?: string;
  avatarUrl?: string;
  isActive: boolean;
  emailVerified: boolean;
  phoneVerified: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
  deviceFingerprint?: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  password: string;
  confirmPassword: string;
  role: string;
  deviceFingerprint?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

// Company Types
export interface Company {
  id: string;
  name: string;
  taxId: string;
  type: 'GENERAL_CONTRACTOR' | 'SUB_CONTRACTOR' | 'DEVELOPER' | 'CONSULTANT';
  status: 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'INACTIVE';
  address?: string;
  city?: string;
  country?: string;
  phoneNumber?: string;
  email?: string;
  website?: string;
  logoUrl?: string;
  description?: string;
  adminUserId: string;
  createdAt: string;
  updatedAt: string;
}

// Project Types
export interface Project {
  id: string;
  name: string;
  description?: string;
  companyId: string;
  status: 'PLANNING' | 'IN_PROGRESS' | 'ON_HOLD' | 'COMPLETED' | 'CANCELLED';
  startDate?: string;
  endDate?: string;
  budget?: number;
  location?: ProjectLocation;
  phases: ProjectPhase[];
  progress: number;
  createdAt: string;
  updatedAt: string;
}

export interface ProjectLocation {
  id: string;
  projectId: string;
  address: string;
  city: string;
  country: string;
  latitude: number;
  longitude: number;
  placeId?: string;
}

export interface ProjectPhase {
  id: string;
  name: string;
  description?: string;
  startDate?: string;
  endDate?: string;
  progress: number;
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED';
  order: number;
}

// Task Types
export interface Task {
  id: string;
  title: string;
  description?: string;
  companyId: string;
  projectId?: string;
  creatorId: string;
  assignedToId?: string;
  status: 'TODO' | 'IN_PROGRESS' | 'REVIEW' | 'COMPLETED' | 'CANCELLED';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  type: 'GENERAL' | 'INSPECTION' | 'MAINTENANCE' | 'REPAIR' | 'INSTALLATION';
  dueDate?: string;
  startDate?: string;
  completedDate?: string;
  progress: number;
  estimatedHours?: number;
  actualHours?: number;
  tags?: string[];
  createdAt: string;
  updatedAt: string;
}

// Issue Types
export interface Issue {
  id: string;
  title: string;
  description?: string;
  companyId: string;
  projectId?: string;
  taskId?: string;
  reportedById: string;
  assignedToId?: string;
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED' | 'REOPENED';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  severity: 'MINOR' | 'MAJOR' | 'CRITICAL' | 'BLOCKER';
  category?: string;
  dueDate?: string;
  resolvedDate?: string;
  createdAt: string;
  updatedAt: string;
}

// Unit Types
export interface Unit {
  id: string;
  projectId: string;
  unitNumber: string;
  blockName?: string;
  floor?: number;
  type: 'APARTMENT' | 'VILLA' | 'OFFICE' | 'SHOP' | 'WAREHOUSE';
  status: 'PLANNED' | 'UNDER_CONSTRUCTION' | 'COMPLETED' | 'DELIVERED';
  saleStatus: 'AVAILABLE' | 'RESERVED' | 'SOLD' | 'NOT_FOR_SALE';
  grossArea?: number;
  netArea?: number;
  rooms?: number;
  bathrooms?: number;
  price?: number;
  ownerId?: string;
  reservedDate?: string;
  soldDate?: string;
  progress: number;
  createdAt: string;
  updatedAt: string;
}

// Document Types
export interface Document {
  id: string;
  fileName: string;
  originalFileName: string;
  fileSize: number;
  mimeType: string;
  entityType: 'PROJECT' | 'TASK' | 'ISSUE' | 'UNIT' | 'USER' | 'COMPANY';
  entityId: string;
  companyId: string;
  uploadedById: string;
  tags?: string[];
  description?: string;
  isArchived: boolean;
  downloadUrl?: string;
  uploadedAt: string;
  updatedAt: string;
}

// Notification Types
export interface Notification {
  id: string;
  userId: string;
  type: 'INFO' | 'SUCCESS' | 'WARNING' | 'ERROR' | 'TASK_ASSIGNED' | 'ISSUE_ASSIGNED';
  title: string;
  message: string;
  read: boolean;
  actionUrl?: string;
  createdAt: string;
}

// Pagination Types
export interface PageRequest {
  page?: number;
  size?: number;
  sort?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// Statistics Types
export interface DashboardStats {
  totalProjects: number;
  activeProjects: number;
  completedProjects: number;
  totalTasks: number;
  completedTasks: number;
  overdueTasks: number;
  totalIssues: number;
  openIssues: number;
  totalUnits: number;
  availableUnits: number;
  soldUnits: number;
}
