import DashboardLayout from '@/components/DashboardLayout'
import { Star, MapPin, Award } from 'lucide-react'

export default function WorkersPage() {
  const workers = [
    { id: 1, name: 'Elnur Mammadov', city: 'Baku', specialty: 'Electrician', rating: 4.8, availability: 'Available' },
    { id: 2, name: 'Leyla Huseynova', city: 'Ganja', specialty: 'Architect', rating: 4.9, availability: 'Busy' },
    { id: 3, name: 'Rashad Aliyev', city: 'Shusha', specialty: 'Mason', rating: 4.7, availability: 'Available' },
    { id: 4, name: 'Aygun Hasanova', city: 'Baku', specialty: 'Engineer', rating: 4.9, availability: 'Available' },
    { id: 5, name: 'Farid Ismayilov', city: 'Sumgait', specialty: 'Plumber', rating: 4.6, availability: 'On Leave' },
    { id: 6, name: 'Nigar Ahmadova', city: 'Khirdalan', specialty: 'Painter', rating: 4.8, availability: 'Available' },
  ]

  const topWorkers = [
    { name: 'Aygun Hasanova', specialty: 'Engineer', rating: 4.9, projects: 15 },
    { name: 'Leyla Huseynova', specialty: 'Architect', rating: 4.9, projects: 12 },
    { name: 'Elnur Mammadov', specialty: 'Electrician', rating: 4.8, projects: 18 },
  ]

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-dark">Workers</h1>
          <p className="text-gray-600 mt-1">Manage your construction workforce</p>
        </div>

        {/* Top Workers Section */}
        <div>
          <h2 className="text-xl font-bold mb-4 flex items-center">
            <Award className="w-6 h-6 mr-2 text-yellow-500" />
            Top Performers
          </h2>
          <div className="grid md:grid-cols-3 gap-6">
            {topWorkers.map((worker, index) => (
              <div key={index} className="card bg-gradient-to-br from-primary/10 to-blue-50">
                <div className="flex items-center justify-between mb-3">
                  <div className="w-12 h-12 bg-primary rounded-full flex items-center justify-center text-white font-bold">
                    {worker.name.split(' ').map(n => n[0]).join('')}
                  </div>
                  <div className="flex items-center bg-yellow-100 px-2 py-1 rounded">
                    <Star className="w-4 h-4 text-yellow-500 mr-1" fill="currentColor" />
                    <span className="font-semibold">{worker.rating}</span>
                  </div>
                </div>
                <h3 className="font-bold text-lg">{worker.name}</h3>
                <p className="text-gray-600 text-sm">{worker.specialty}</p>
                <p className="text-sm text-gray-500 mt-2">{worker.projects} projects completed</p>
              </div>
            ))}
          </div>
        </div>

        {/* Filter Bar */}
        <div className="card">
          <div className="flex flex-wrap gap-4">
            <select className="input max-w-xs">
              <option>All Cities</option>
              <option>Baku</option>
              <option>Ganja</option>
              <option>Shusha</option>
              <option>Sumgait</option>
            </select>
            <select className="input max-w-xs">
              <option>All Specialties</option>
              <option>Electrician</option>
              <option>Plumber</option>
              <option>Mason</option>
              <option>Engineer</option>
              <option>Architect</option>
            </select>
            <select className="input max-w-xs">
              <option>All Ratings</option>
              <option>4.5+ Stars</option>
              <option>4.0+ Stars</option>
              <option>3.5+ Stars</option>
            </select>
          </div>
        </div>

        {/* Workers Table */}
        <div className="card overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-200">
                <th className="text-left py-3 px-4 font-semibold text-gray-700">Name</th>
                <th className="text-left py-3 px-4 font-semibold text-gray-700">City</th>
                <th className="text-left py-3 px-4 font-semibold text-gray-700">Specialty</th>
                <th className="text-left py-3 px-4 font-semibold text-gray-700">Rating</th>
                <th className="text-left py-3 px-4 font-semibold text-gray-700">Availability</th>
                <th className="text-left py-3 px-4 font-semibold text-gray-700">Actions</th>
              </tr>
            </thead>
            <tbody>
              {workers.map((worker) => (
                <tr key={worker.id} className="border-b border-gray-100 hover:bg-gray-50">
                  <td className="py-4 px-4">
                    <div className="flex items-center">
                      <div className="w-10 h-10 bg-primary rounded-full flex items-center justify-center text-white font-semibold mr-3">
                        {worker.name.split(' ').map(n => n[0]).join('')}
                      </div>
                      <span className="font-medium">{worker.name}</span>
                    </div>
                  </td>
                  <td className="py-4 px-4">
                    <div className="flex items-center text-gray-600">
                      <MapPin className="w-4 h-4 mr-1" />
                      {worker.city}
                    </div>
                  </td>
                  <td className="py-4 px-4">{worker.specialty}</td>
                  <td className="py-4 px-4">
                    <div className="flex items-center">
                      <Star className="w-4 h-4 text-yellow-500 mr-1" fill="currentColor" />
                      <span className="font-semibold">{worker.rating}</span>
                    </div>
                  </td>
                  <td className="py-4 px-4">
                    <span className={`px-3 py-1 rounded-full text-sm font-medium ${
                      worker.availability === 'Available'
                        ? 'bg-green-100 text-green-700'
                        : worker.availability === 'Busy'
                        ? 'bg-red-100 text-red-700'
                        : 'bg-gray-100 text-gray-700'
                    }`}>
                      {worker.availability}
                    </span>
                  </td>
                  <td className="py-4 px-4">
                    <button className="btn-primary text-sm">
                      Add to Project
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
