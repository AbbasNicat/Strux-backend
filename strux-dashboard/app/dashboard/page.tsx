'use client'

import { useState, useEffect } from 'react'
import DashboardLayout from '@/components/DashboardLayout'
import Map from '@/components/Map'
import { Building2, CheckCircle, Users, Home } from 'lucide-react'
import { projectService } from '@/services/project.service'
import { taskService } from '@/services/task.service'
import { workerService } from '@/services/user.service'
import { unitService } from '@/services/unit.service'

export default function DashboardPage() {
  const [stats, setStats] = useState({
    activeProjects: 0,
    tasks: 0,
    workers: 0,
    completedUnits: 0,
  })
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadDashboardData()
  }, [])

  const loadDashboardData = async () => {
    try {
      // For demo, using mock company ID - in production this would come from auth context
      const companyId = localStorage.getItem('companyId') || 'demo-company-id'

      const [projects, tasks, workers] = await Promise.all([
        projectService.getAllProjects({ page: 0, size: 100 }).catch(() => ({ content: [] })),
        taskService.getCompanyTasks(companyId).catch(() => []),
        workerService.getCompanyWorkers(companyId).catch(() => []),
      ])

      setStats({
        activeProjects: projects.content?.filter((p: any) => p.status === 'IN_PROGRESS').length || 0,
        tasks: tasks.length || 0,
        workers: workers.length || 0,
        completedUnits: 0, // Will be calculated from all projects
      })
    } catch (error) {
      console.error('Failed to load dashboard data:', error)
    } finally {
      setLoading(false)
    }
  }

  const statItems = [
    { name: 'Active Projects', value: stats.activeProjects, icon: Building2, color: 'bg-blue-500' },
    { name: 'Tasks', value: stats.tasks, icon: CheckCircle, color: 'bg-green-500' },
    { name: 'Workers', value: stats.workers, icon: Users, color: 'bg-purple-500' },
    { name: 'Completed Units', value: stats.completedUnits, icon: Home, color: 'bg-orange-500' },
  ]

  if (loading) {
    return (
      <DashboardLayout>
        <div className="flex items-center justify-center h-96">
          <div className="text-center">
            <div className="w-16 h-16 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
            <p className="text-gray-600">Loading dashboard...</p>
          </div>
        </div>
      </DashboardLayout>
    )
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-dark">Dashboard</h1>
          <p className="text-gray-600 mt-1">Overview of your construction projects</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {statItems.map((stat) => {
            const Icon = stat.icon
            return (
              <div key={stat.name} className="card">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-gray-600">{stat.name}</p>
                    <p className="text-3xl font-bold mt-2">{stat.value}</p>
                  </div>
                  <div className={`${stat.color} w-12 h-12 rounded-lg flex items-center justify-center`}>
                    <Icon className="w-6 h-6 text-white" />
                  </div>
                </div>
              </div>
            )
          })}
        </div>

        <div className="grid lg:grid-cols-2 gap-6">
          <div className="card">
            <h2 className="text-xl font-bold mb-4">Recent Activity</h2>
            <p className="text-gray-500">No recent activity</p>
          </div>

          <div className="card">
            <h2 className="text-xl font-bold mb-4">Project Locations</h2>
            <Map />
          </div>
        </div>
      </div>
    </DashboardLayout>
  )
}
