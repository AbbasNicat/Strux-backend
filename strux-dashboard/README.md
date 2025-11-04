# Strux Dashboard

Modern web dashboard UI for a construction project monitoring platform.

## 🧱 About Strux

Strux is a smart construction management and monitoring platform for Azerbaijan and Karabakh reconstruction projects. It allows companies, engineers, and workers to track progress, manage projects, assign tasks, and visualize project data on maps.

## 🎨 Features

- **Modern Design**: Clean, minimalistic UI with rounded cards and soft shadows
- **Responsive**: Fully responsive design that works on all devices
- **Interactive Maps**: Real-time project location visualization using Leaflet
- **Dashboard Analytics**: Comprehensive overview with stats and recent activity
- **Project Management**: Track multiple projects with progress indicators
- **Worker Management**: Manage workforce, track performance and availability
- **Task Management**: Assign and monitor tasks with priority levels
- **Real-time Notifications**: Stay updated with email, SMS, and push notifications
- **Profile & Settings**: Customizable user profiles and account settings

## 📍 Pages

1. **Landing Page** (`/`) - Hero section with features and CTA
2. **Login** (`/login`) - User authentication
3. **Register** (`/register`) - New user registration
4. **Dashboard** (`/dashboard`) - Main dashboard with stats and map
5. **Projects** (`/projects`) - Project management grid
6. **Workers** (`/workers`) - Worker management table
7. **Units** (`/units`) - Construction units overview
8. **Tasks** (`/tasks`) - Task management data table
9. **Notifications** (`/notifications`) - Notification center
10. **Profile** (`/profile`) - User profile management
11. **Settings** (`/settings`) - Account settings
12. **Logout** (`/logout`) - Logout confirmation

## 🚀 Getting Started

### Prerequisites

- Node.js 18+ installed
- npm or yarn package manager

### Installation

1. Install dependencies:
```bash
npm install
```

2. Run the development server:
```bash
npm run dev
```

3. Open [http://localhost:3000](http://localhost:3000) in your browser

### Build for Production

```bash
npm run build
npm start
```

## 🎨 Design System

### Colors

- Primary Blue: `#2563eb`
- Light Gray: `#f3f4f6`
- Dark Gray: `#111827`
- White: `#ffffff`

### Components

- Rounded cards with soft shadows
- Modern navigation with sidebar
- Interactive data visualizations
- Responsive grid layouts
- Custom form inputs with focus states

## 🛠️ Tech Stack

- **Framework**: Next.js 16 with App Router
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **Maps**: Leaflet & React Leaflet
- **Icons**: Lucide React
- **Deployment**: Vercel-ready

## 📄 License

© 2025 Strux Platform. All rights reserved.

## 🔌 API Integration

The frontend is now fully integrated with the backend microservices:

### API Services

All backend endpoints are accessible through service classes:
- `authService` - Authentication (login, register, logout)
- `userService` - User management
- `workerService` - Worker operations
- `projectService` - Project management
- `taskService` - Task management
- `unitService` - Unit/property management
- `notificationService` - Notifications
- `companyService` - Company operations

### Configuration

Set your API Gateway URL in `.env.local`:

```env
NEXT_PUBLIC_API_GATEWAY=http://localhost:8081
```

### Authentication

The app uses JWT token-based authentication:
- Tokens are stored in localStorage
- AuthContext provides global auth state
- Protected routes check authentication status
- Automatic token refresh (when implemented on backend)

### Usage

```typescript
import { authService } from '@/services/auth.service'
import { projectService } from '@/services/project.service'

// Login
await authService.login({ email, password })

// Get projects
const projects = await projectService.getAllProjects()
```

## 🏗️ Backend Services

The frontend connects to these microservices via API Gateway (Port 8081):

1. **Auth Service** (9091) - `/api/auth/**`
2. **User Service** (9093) - `/api/users/**`, `/api/workers/**`
3. **Company Service** (9094) - `/api/companies/**`
4. **Document Service** (9089) - `/api/documents/**`
5. **Project Service** (9095) - `/api/projects/**`, `/api/locations/**`
6. **Task Service** (9096) - `/api/tasks/**`
7. **Notification Service** (9098) - `/api/notifications/**`
8. **Unit Service** (9099) - `/api/units/**`

All requests go through the API Gateway at `localhost:8081`.
