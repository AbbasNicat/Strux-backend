'use client'

import DashboardLayout from '@/components/DashboardLayout'
import { User, MapPin, Briefcase, Star, Upload } from 'lucide-react'
import { useState } from 'react'

export default function ProfilePage() {
  const [isEditing, setIsEditing] = useState(false)

  const performanceData = [
    { month: 'Jan', projects: 4 },
    { month: 'Feb', projects: 5 },
    { month: 'Mar', projects: 3 },
    { month: 'Apr', projects: 6 },
    { month: 'May', projects: 5 },
    { month: 'Jun', projects: 7 },
  ]

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-dark">Profile</h1>
          <p className="text-gray-600 mt-1">Manage your account information</p>
        </div>

        <div className="grid lg:grid-cols-3 gap-6">
          {/* Profile Card */}
          <div className="card text-center">
            <div className="relative inline-block mb-4">
              <div className="w-32 h-32 bg-gradient-to-br from-primary to-blue-600 rounded-full flex items-center justify-center text-white text-4xl font-bold mx-auto">
                AU
              </div>
              <button className="absolute bottom-0 right-0 w-10 h-10 bg-white rounded-full shadow-lg flex items-center justify-center hover:bg-gray-50 transition-all">
                <Upload className="w-5 h-5 text-gray-600" />
              </button>
            </div>
            <h2 className="text-2xl font-bold">Admin User</h2>
            <p className="text-gray-600 mt-1">admin@strux.az</p>

            <div className="mt-6 space-y-3">
              <div className="flex items-center justify-center text-gray-600">
                <MapPin className="w-4 h-4 mr-2" />
                <span>Baku, Azerbaijan</span>
              </div>
              <div className="flex items-center justify-center text-gray-600">
                <Briefcase className="w-4 h-4 mr-2" />
                <span>Project Manager</span>
              </div>
              <div className="flex items-center justify-center text-yellow-500">
                <Star className="w-4 h-4 mr-2" fill="currentColor" />
                <span className="font-semibold">4.9 Rating</span>
              </div>
            </div>

            <button
              onClick={() => setIsEditing(!isEditing)}
              className="btn-primary w-full mt-6"
            >
              {isEditing ? 'Cancel' : 'Edit Profile'}
            </button>
          </div>

          {/* Profile Information */}
          <div className="lg:col-span-2 card">
            <h2 className="text-xl font-bold mb-6">Personal Information</h2>
            <form className="space-y-4">
              <div className="grid md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    First Name
                  </label>
                  <input
                    type="text"
                    defaultValue="Admin"
                    disabled={!isEditing}
                    className="input disabled:bg-gray-100"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Last Name
                  </label>
                  <input
                    type="text"
                    defaultValue="User"
                    disabled={!isEditing}
                    className="input disabled:bg-gray-100"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Email
                </label>
                <input
                  type="email"
                  defaultValue="admin@strux.az"
                  disabled={!isEditing}
                  className="input disabled:bg-gray-100"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Phone Number
                </label>
                <input
                  type="tel"
                  defaultValue="+994 50 123 45 67"
                  disabled={!isEditing}
                  className="input disabled:bg-gray-100"
                />
              </div>

              <div className="grid md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    City
                  </label>
                  <select
                    disabled={!isEditing}
                    className="input disabled:bg-gray-100"
                  >
                    <option>Baku</option>
                    <option>Ganja</option>
                    <option>Sumgait</option>
                    <option>Shusha</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Specialty
                  </label>
                  <select
                    disabled={!isEditing}
                    className="input disabled:bg-gray-100"
                  >
                    <option>Project Manager</option>
                    <option>Engineer</option>
                    <option>Architect</option>
                    <option>Supervisor</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Bio
                </label>
                <textarea
                  rows={4}
                  defaultValue="Experienced project manager with 10+ years in construction industry..."
                  disabled={!isEditing}
                  className="input disabled:bg-gray-100"
                />
              </div>

              {isEditing && (
                <div className="flex space-x-4">
                  <button type="submit" className="btn-primary">
                    Save Changes
                  </button>
                  <button
                    type="button"
                    onClick={() => setIsEditing(false)}
                    className="btn-secondary"
                  >
                    Cancel
                  </button>
                </div>
              )}
            </form>
          </div>
        </div>

        {/* Performance Chart */}
        <div className="card">
          <h2 className="text-xl font-bold mb-6">Performance Overview</h2>
          <div className="grid grid-cols-6 gap-4 items-end h-64">
            {performanceData.map((data, index) => (
              <div key={index} className="flex flex-col items-center">
                <div className="w-full bg-primary/20 rounded-t-lg relative" style={{ height: `${data.projects * 30}px` }}>
                  <div className="absolute -top-8 left-1/2 transform -translate-x-1/2 bg-primary text-white text-sm font-semibold px-2 py-1 rounded">
                    {data.projects}
                  </div>
                  <div className="w-full h-full bg-primary rounded-t-lg"></div>
                </div>
                <span className="text-sm text-gray-600 mt-2">{data.month}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </DashboardLayout>
  )
}
