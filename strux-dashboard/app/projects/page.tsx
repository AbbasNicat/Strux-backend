import DashboardLayout from '@/components/DashboardLayout'
import { Plus, MapPin, Clock } from 'lucide-react'

export default function ProjectsPage() {
  const projects = [
    { id: 1, name: 'Shusha Reconstruction', status: 75, city: 'Shusha', company: 'AzBuild LLC', lastUpdated: '2 hours ago' },
    { id: 2, name: 'Baku Tower Complex', status: 60, city: 'Baku', company: 'Modern Construction', lastUpdated: '5 hours ago' },
    { id: 3, name: 'Ganja Residential', status: 90, city: 'Ganja', company: 'HomeBuilder Co', lastUpdated: '1 day ago' },
    { id: 4, name: 'Khirdalan Phase 2', status: 45, city: 'Khirdalan', company: 'AzBuild LLC', lastUpdated: '3 hours ago' },
    { id: 5, name: 'Sumgait Industrial', status: 30, city: 'Sumgait', company: 'Industrial Builders', lastUpdated: '6 hours ago' },
    { id: 6, name: 'Agdam Renewal', status: 15, city: 'Agdam', company: 'Reconstruction Group', lastUpdated: '1 day ago' },
  ]

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

        {/* Filter Bar */}
        <div className="card">
          <div className="flex flex-wrap gap-4">
            <select className="input max-w-xs">
              <option>All Status</option>
              <option>In Progress</option>
              <option>Completed</option>
              <option>On Hold</option>
            </select>
            <select className="input max-w-xs">
              <option>All Locations</option>
              <option>Baku</option>
              <option>Shusha</option>
              <option>Ganja</option>
              <option>Agdam</option>
            </select>
            <select className="input max-w-xs">
              <option>All Companies</option>
              <option>AzBuild LLC</option>
              <option>Modern Construction</option>
              <option>HomeBuilder Co</option>
            </select>
          </div>
        </div>

        {/* Projects Grid */}
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          {projects.map((project) => (
            <div key={project.id} className="card hover:shadow-xl transition-all cursor-pointer">
              <h3 className="text-xl font-bold text-gray-dark mb-3">{project.name}</h3>

              <div className="space-y-3 mb-4">
                <div className="flex items-center text-gray-600">
                  <MapPin className="w-4 h-4 mr-2" />
                  <span className="text-sm">{project.city}</span>
                </div>
                <div className="flex items-center text-gray-600">
                  <Clock className="w-4 h-4 mr-2" />
                  <span className="text-sm">Updated {project.lastUpdated}</span>
                </div>
              </div>

              <div className="mb-2">
                <div className="flex justify-between text-sm mb-1">
                  <span className="text-gray-600">Progress</span>
                  <span className="font-semibold text-primary">{project.status}%</span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div
                    className="bg-primary h-2 rounded-full transition-all"
                    style={{ width: `${project.status}%` }}
                  />
                </div>
              </div>

              <div className="mt-4 pt-4 border-t border-gray-200">
                <p className="text-sm text-gray-600">{project.company}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </DashboardLayout>
  )
}
