'use client'

import { useState, useEffect } from 'react'
import DashboardLayout from '@/components/DashboardLayout'
import { Plus, MapPin, Clock } from 'lucide-react'
import { projectService } from '@/services/project.service'
import { Project } from '@/lib/types'

export default function ProjectsPage() {
  const [projects, setProjects] = useState<Project[]>([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState({ status: '', location: '', company: '' })

  useEffect(() => {
    loadProjects()
  }, [])

  const loadProjects = async () => {
    try {
      const response = await projectService.getAllProjects({ page: 0, size: 50 })
      setProjects(response.content || [])
    } catch (error) {
      console.error('Failed to load projects:', error)
    } finally {
      setLoading(false)
    }
  }

  const getTimeAgo = (date: string) => {
    const now = new Date()
    const updated = new Date(date)
    const diff = Math.floor((now.getTime() - updated.getTime()) / (1000 * 60 * 60))
    if (diff < 1) return 'Just now'
    if (diff < 24) return `${diff} hours ago`
    const days = Math.floor(diff / 24)
    return `${days} days ago`
  }

  if (loading) {
    return (
      <DashboardLayout>
        <div className="flex items-center justify-center h-96">
          <div className="w-16 h-16 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
        </div>
      </DashboardLayout>
    )
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-dark">Projects</h1>
            <p className="text-gray-600 mt-1">Manage all construction projects</p>
          </div>
          <button className="btn-primary flex items-center space-x-2">
            <Plus className="w-5 h-5" />
            <span>Add Project</span>
          </button>
        </div>

        <div className="card">
          <div className="flex flex-wrap gap-4">
            <select className="input max-w-xs" onChange={(e) => setFilter({ ...filter, status: e.target.value })}>
              <option value="">All Status</option>
              <option value="PLANNING">Planning</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="COMPLETED">Completed</option>
              <option value="ON_HOLD">On Hold</option>
            </select>
            <select className="input max-w-xs" onChange={(e) => setFilter({ ...filter, location: e.target.value })}>
              <option value="">All Locations</option>
              <option value="Baku">Baku</option>
              <option value="Shusha">Shusha</option>
              <option value="Ganja">Ganja</option>
            </select>
          </div>
        </div>

        {projects.length === 0 ? (
          <div className="card text-center py-12">
            <p className="text-gray-500">No projects found. Create your first project to get started.</p>
          </div>
        ) : (
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {projects.map((project) => (
              <div key={project.id} className="card hover:shadow-xl transition-all cursor-pointer">
                <h3 className="text-xl font-bold text-gray-dark mb-3">{project.name}</h3>
                <div className="space-y-3 mb-4">
                  <div className="flex items-center text-gray-600">
                    <MapPin className="w-4 h-4 mr-2" />
                    <span className="text-sm">{project.city || 'N/A'}</span>
                  </div>
                  <div className="flex items-center text-gray-600">
                    <Clock className="w-4 h-4 mr-2" />
                    <span className="text-sm">Updated {getTimeAgo(project.updatedAt)}</span>
                  </div>
                </div>
                <div className="mb-2">
                  <div className="flex justify-between text-sm mb-1">
                    <span className="text-gray-600">Progress</span>
                    <span className="font-semibold text-primary">{project.progress}%</span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div
                      className="bg-primary h-2 rounded-full transition-all"
                      style={{ width: `${project.progress}%` }}
                    />
                  </div>
                </div>
                <div className="mt-4 pt-4 border-t border-gray-200">
                  <p className="text-sm text-gray-600">{project.status.replace('_', ' ')}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </DashboardLayout>
  )
}
