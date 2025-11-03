import DashboardLayout from '@/components/DashboardLayout'
import { Home } from 'lucide-react'

export default function UnitsPage() {
  const units = [
    { id: 1, number: 'A-101', floor: 1, type: '2+1', progress: 100, status: 'Sold' },
    { id: 2, number: 'A-102', floor: 1, type: '3+1', progress: 100, status: 'Sold' },
    { id: 3, number: 'A-201', floor: 2, type: '2+1', progress: 85, status: 'Reserved' },
    { id: 4, number: 'A-202', floor: 2, type: '3+1', progress: 75, status: 'Available' },
    { id: 5, number: 'A-301', floor: 3, type: '2+1', progress: 60, status: 'Available' },
    { id: 6, number: 'A-302', floor: 3, type: '3+1', progress: 60, status: 'Available' },
    { id: 7, number: 'B-101', floor: 1, type: '4+1', progress: 45, status: 'Available' },
    { id: 8, number: 'B-102', floor: 1, type: '2+1', progress: 40, status: 'Available' },
    { id: 9, number: 'B-201', floor: 2, type: '3+1', progress: 30, status: 'Reserved' },
  ]

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'Sold':
        return 'bg-red-100 text-red-700 border-red-200'
      case 'Reserved':
        return 'bg-yellow-100 text-yellow-700 border-yellow-200'
      case 'Available':
        return 'bg-green-100 text-green-700 border-green-200'
      default:
        return 'bg-gray-100 text-gray-700 border-gray-200'
    }
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-dark">Units</h1>
          <p className="text-gray-600 mt-1">Residential and commercial units overview</p>
        </div>

        {/* Filter Bar */}
        <div className="card">
          <div className="flex flex-wrap gap-4">
            <select className="input max-w-xs">
              <option>All Floors</option>
              <option>Floor 1</option>
              <option>Floor 2</option>
              <option>Floor 3</option>
              <option>Floor 4+</option>
            </select>
            <select className="input max-w-xs">
              <option>All Types</option>
              <option>2+1</option>
              <option>3+1</option>
              <option>4+1</option>
            </select>
            <select className="input max-w-xs">
              <option>All Status</option>
              <option>Available</option>
              <option>Reserved</option>
              <option>Sold</option>
            </select>
          </div>
        </div>

        {/* Units Grid */}
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          {units.map((unit) => (
            <div key={unit.id} className="card hover:shadow-xl transition-all">
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center space-x-3">
                  <div className="w-12 h-12 bg-primary/10 rounded-lg flex items-center justify-center">
                    <Home className="w-6 h-6 text-primary" />
                  </div>
                  <div>
                    <h3 className="text-xl font-bold text-gray-dark">{unit.number}</h3>
                    <p className="text-sm text-gray-600">Floor {unit.floor}</p>
                  </div>
                </div>
                <span className={`px-3 py-1 rounded-full text-xs font-medium border ${getStatusColor(unit.status)}`}>
                  {unit.status}
                </span>
              </div>

              <div className="space-y-3">
                <div className="flex justify-between text-sm">
                  <span className="text-gray-600">Type:</span>
                  <span className="font-semibold">{unit.type}</span>
                </div>

                <div>
                  <div className="flex justify-between text-sm mb-2">
                    <span className="text-gray-600">Construction Progress</span>
                    <span className="font-semibold text-primary">{unit.progress}%</span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div
                      className={`h-2 rounded-full transition-all ${
                        unit.progress === 100 ? 'bg-green-500' : 'bg-primary'
                      }`}
                      style={{ width: `${unit.progress}%` }}
                    />
                  </div>
                </div>

                <div className="pt-3 border-t border-gray-200 flex justify-between">
                  <button className="text-primary hover:underline text-sm font-medium">
                    View Details
                  </button>
                  {unit.status === 'Available' && (
                    <button className="text-green-600 hover:underline text-sm font-medium">
                      Mark Reserved
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Summary */}
        <div className="card bg-gradient-to-r from-primary/5 to-blue-50">
          <div className="grid grid-cols-3 gap-6 text-center">
            <div>
              <p className="text-3xl font-bold text-green-600">
                {units.filter(u => u.status === 'Available').length}
              </p>
              <p className="text-gray-600 mt-1">Available</p>
            </div>
            <div>
              <p className="text-3xl font-bold text-yellow-600">
                {units.filter(u => u.status === 'Reserved').length}
              </p>
              <p className="text-gray-600 mt-1">Reserved</p>
            </div>
            <div>
              <p className="text-3xl font-bold text-red-600">
                {units.filter(u => u.status === 'Sold').length}
              </p>
              <p className="text-gray-600 mt-1">Sold</p>
            </div>
          </div>
        </div>
      </div>
    </DashboardLayout>
  )
}
