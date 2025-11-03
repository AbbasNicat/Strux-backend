import DashboardLayout from '@/components/DashboardLayout'
import Map from '@/components/Map'
import { Building2, CheckCircle, Users, Home } from 'lucide-react'

export default function DashboardPage() {
  const stats = [
    { name: 'Active Projects', value: '12', icon: Building2, color: 'bg-blue-500' },
    { name: 'Tasks', value: '48', icon: CheckCircle, color: 'bg-green-500' },
    { name: 'Workers', value: '156', icon: Users, color: 'bg-purple-500' },
    { name: 'Completed Units', value: '89', icon: Home, color: 'bg-orange-500' },
  ]

  const recentActivity = [
    { id: 1, action: 'New project created', project: 'Shusha Reconstruction', time: '2 hours ago' },
    { id: 2, action: 'Task completed', project: 'Baku Development', time: '4 hours ago' },
    { id: 3, action: 'Worker assigned', project: 'Khirdalan Phase 2', time: '6 hours ago' },
    { id: 4, action: 'Unit inspection passed', project: 'Ganja Heights', time: '8 hours ago' },
  ]

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-dark">Dashboard</h1>
          <p className="text-gray-600 mt-1">Overview of your construction projects</p>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {stats.map((stat) => {
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

        {/* Recent Activity and Map */}
        <div className="grid lg:grid-cols-2 gap-6">
          {/* Recent Activity */}
          <div className="card">
            <h2 className="text-xl font-bold mb-4">Recent Activity</h2>
            <div className="space-y-4">
              {recentActivity.map((activity) => (
                <div key={activity.id} className="flex items-start space-x-3 pb-4 border-b border-gray-200 last:border-0">
                  <div className="w-2 h-2 bg-primary rounded-full mt-2"></div>
                  <div className="flex-1">
                    <p className="font-medium text-gray-dark">{activity.action}</p>
                    <p className="text-sm text-gray-600">{activity.project}</p>
                    <p className="text-xs text-gray-500 mt-1">{activity.time}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Map Preview */}
          <div className="card">
            <h2 className="text-xl font-bold mb-4">Project Locations</h2>
            <Map />
          </div>
        </div>
      </div>
    </DashboardLayout>
  )
}
