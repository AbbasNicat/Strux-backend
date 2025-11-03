import Link from 'next/link'
import { Building2, MapPin, Users, Bell } from 'lucide-react'

export default function HomePage() {
  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <div
        className="relative h-screen flex items-center justify-center bg-cover bg-center"
        style={{
          backgroundImage: 'linear-gradient(rgba(17, 24, 39, 0.7), rgba(17, 24, 39, 0.7)), url(https://images.unsplash.com/photo-1541888946425-d81bb19240f5?w=1920)',
        }}
      >
        <div className="text-center text-white z-10 px-4">
          <div className="flex items-center justify-center mb-6">
            <Building2 className="w-16 h-16 text-primary" />
          </div>
          <h1 className="text-6xl font-bold mb-4">Strux</h1>
          <p className="text-2xl mb-8 text-gray-200">
            Smart Construction Management Platform
          </p>
          <p className="text-lg mb-8 max-w-2xl mx-auto text-gray-300">
            Monitor and manage construction projects across Azerbaijan and Karabakh.
            Track progress, assign tasks, and visualize data in real-time.
          </p>
          <Link
            href="/dashboard"
            className="inline-block bg-primary text-white px-8 py-4 rounded-lg text-lg font-semibold hover:bg-blue-700 transition-all shadow-lg"
          >
            Explore Dashboard
          </Link>
        </div>
      </div>

      {/* Features Section */}
      <div className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4">
          <h2 className="text-4xl font-bold text-center mb-16 text-gray-dark">
            Platform Features
          </h2>
          <div className="grid md:grid-cols-3 gap-8">
            {/* Feature 1 */}
            <div className="card text-center">
              <div className="flex justify-center mb-4">
                <div className="w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center">
                  <Building2 className="w-8 h-8 text-primary" />
                </div>
              </div>
              <h3 className="text-xl font-bold mb-3">Project Management</h3>
              <p className="text-gray-600">
                Track multiple construction projects, monitor progress, and manage
                resources efficiently from a single dashboard.
              </p>
            </div>

            {/* Feature 2 */}
            <div className="card text-center">
              <div className="flex justify-center mb-4">
                <div className="w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center">
                  <Users className="w-8 h-8 text-primary" />
                </div>
              </div>
              <h3 className="text-xl font-bold mb-3">Worker Performance</h3>
              <p className="text-gray-600">
                Monitor worker performance, track availability, and assign tasks
                based on specialty and location.
              </p>
            </div>

            {/* Feature 3 */}
            <div className="card text-center">
              <div className="flex justify-center mb-4">
                <div className="w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center">
                  <Bell className="w-8 h-8 text-primary" />
                </div>
              </div>
              <h3 className="text-xl font-bold mb-3">Real-time Notifications</h3>
              <p className="text-gray-600">
                Stay updated with instant notifications via email, SMS, and push
                notifications for critical updates.
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Footer */}
      <footer className="bg-gray-dark text-white py-8">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <div className="flex items-center justify-center mb-4">
            <Building2 className="w-8 h-8 text-primary mr-2" />
            <span className="text-xl font-bold">Strux</span>
          </div>
          <p className="text-gray-400">
            © 2025 Strux Platform. All rights reserved.
          </p>
        </div>
      </footer>
    </div>
  )
}
