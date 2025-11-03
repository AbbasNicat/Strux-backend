import DashboardLayout from '@/components/DashboardLayout'
import { Plus, AlertCircle } from 'lucide-react'

export default function TasksPage() {
  const tasks = [
    { id: 1, title: 'Install electrical wiring - Floor 3', assignedTo: 'Elnur Mammadov', priority: 'High', status: 'In Progress', dueDate: '2025-11-05' },
    { id: 2, title: 'Finish plastering - Unit A-201', assignedTo: 'Rashad Aliyev', priority: 'Medium', status: 'Pending', dueDate: '2025-11-07' },
    { id: 3, title: 'Inspect foundation - Building B', assignedTo: 'Aygun Hasanova', priority: 'High', status: 'In Progress', dueDate: '2025-11-04' },
    { id: 4, title: 'Paint exterior walls', assignedTo: 'Nigar Ahmadova', priority: 'Low', status: 'Completed', dueDate: '2025-11-02' },
    { id: 5, title: 'Plumbing installation - Floor 2', assignedTo: 'Farid Ismayilov', priority: 'High', status: 'Pending', dueDate: '2025-11-06' },
    { id: 6, title: 'Quality check - Unit A-102', assignedTo: 'Leyla Huseynova', priority: 'Medium', status: 'Completed', dueDate: '2025-11-01' },
  ]

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'High':
        return 'bg-red-100 text-red-700'
      case 'Medium':
        return 'bg-yellow-100 text-yellow-700'
      case 'Low':
        return 'bg-blue-100 text-blue-700'
      default:
        return 'bg-gray-100 text-gray-700'
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'Completed':
        return 'bg-green-100 text-green-700'
      case 'In Progress':
        return 'bg-blue-100 text-blue-700'
      case 'Pending':
        return 'bg-gray-100 text-gray-700'
      default:
        return 'bg-gray-100 text-gray-700'
    }
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-dark">Tasks</h1>
            <p className="text-gray-600 mt-1">Manage and track project tasks</p>
          </div>
          <button className="btn-primary flex items-center space-x-2">
            <Plus className="w-5 h-5" />
            <span>Add Task</span>
          </button>
        </div>

        {/* Filter and Stats */}
        <div className="grid md:grid-cols-4 gap-4">
          <div className="card bg-gradient-to-br from-blue-50 to-blue-100">
            <p className="text-sm text-blue-700 mb-1">Total Tasks</p>
            <p className="text-2xl font-bold text-blue-900">{tasks.length}</p>
          </div>
          <div className="card bg-gradient-to-br from-green-50 to-green-100">
            <p className="text-sm text-green-700 mb-1">Completed</p>
            <p className="text-2xl font-bold text-green-900">
              {tasks.filter(t => t.status === 'Completed').length}
            </p>
          </div>
          <div className="card bg-gradient-to-br from-yellow-50 to-yellow-100">
            <p className="text-sm text-yellow-700 mb-1">In Progress</p>
            <p className="text-2xl font-bold text-yellow-900">
              {tasks.filter(t => t.status === 'In Progress').length}
            </p>
          </div>
          <div className="card bg-gradient-to-br from-gray-50 to-gray-100">
            <p className="text-sm text-gray-700 mb-1">Pending</p>
            <p className="text-2xl font-bold text-gray-900">
              {tasks.filter(t => t.status === 'Pending').length}
            </p>
          </div>
        </div>

        {/* Tasks Table */}
        <div className="card overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-200">
                <th className="text-left py-3 px-4 font-semibold text-gray-700">Task</th>
                <th className="text-left py-3 px-4 font-semibold text-gray-700">Assigned To</th>
                <th className="text-left py-3 px-4 font-semibold text-gray-700">Priority</th>
                <th className="text-left py-3 px-4 font-semibold text-gray-700">Status</th>
                <th className="text-left py-3 px-4 font-semibold text-gray-700">Due Date</th>
                <th className="text-left py-3 px-4 font-semibold text-gray-700">Actions</th>
              </tr>
            </thead>
            <tbody>
              {tasks.map((task) => (
                <tr key={task.id} className="border-b border-gray-100 hover:bg-gray-50">
                  <td className="py-4 px-4">
                    <div className="flex items-start">
                      {task.priority === 'High' && (
                        <AlertCircle className="w-5 h-5 text-red-500 mr-2 mt-0.5" />
                      )}
                      <span className="font-medium">{task.title}</span>
                    </div>
                  </td>
                  <td className="py-4 px-4 text-gray-600">{task.assignedTo}</td>
                  <td className="py-4 px-4">
                    <span className={`px-3 py-1 rounded-full text-xs font-medium ${getPriorityColor(task.priority)}`}>
                      {task.priority}
                    </span>
                  </td>
                  <td className="py-4 px-4">
                    <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(task.status)}`}>
                      {task.status}
                    </span>
                  </td>
                  <td className="py-4 px-4 text-gray-600">{task.dueDate}</td>
                  <td className="py-4 px-4">
                    <button className="text-primary hover:underline text-sm font-medium">
                      View Details
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </DashboardLayout>
  )
}
