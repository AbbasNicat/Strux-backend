'use client'

import DashboardLayout from '@/components/DashboardLayout'
import { Bell, Mail, Smartphone, CheckCircle, AlertCircle, Info } from 'lucide-react'
import { useState } from 'react'

export default function NotificationsPage() {
  const [preferences, setPreferences] = useState({
    email: true,
    sms: false,
    push: true,
  })

  const notifications = [
    {
      id: 1,
      type: 'success',
      title: 'Task Completed',
      message: 'Electrical wiring installation on Floor 3 has been completed',
      date: '2025-11-03 14:30',
      read: false,
    },
    {
      id: 2,
      type: 'warning',
      title: 'Deadline Approaching',
      message: 'Foundation inspection for Building B is due tomorrow',
      date: '2025-11-03 12:15',
      read: false,
    },
    {
      id: 3,
      type: 'info',
      title: 'New Worker Assigned',
      message: 'Rashad Aliyev has been assigned to Shusha Reconstruction project',
      date: '2025-11-03 09:20',
      read: true,
    },
    {
      id: 4,
      type: 'success',
      title: 'Unit Inspection Passed',
      message: 'Unit A-102 has successfully passed quality inspection',
      date: '2025-11-02 16:45',
      read: true,
    },
    {
      id: 5,
      type: 'info',
      title: 'Project Update',
      message: 'Baku Tower Complex progress updated to 60%',
      date: '2025-11-02 11:30',
      read: true,
    },
  ]

  const getIcon = (type: string) => {
    switch (type) {
      case 'success':
        return <CheckCircle className="w-5 h-5 text-green-500" />
      case 'warning':
        return <AlertCircle className="w-5 h-5 text-yellow-500" />
      case 'info':
        return <Info className="w-5 h-5 text-blue-500" />
      default:
        return <Bell className="w-5 h-5 text-gray-500" />
    }
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-dark">Notifications</h1>
          <p className="text-gray-600 mt-1">Stay updated with project activities</p>
        </div>

        <div className="grid lg:grid-cols-3 gap-6">
          {/* Notifications List */}
          <div className="lg:col-span-2 space-y-3">
            <div className="card">
              <h2 className="text-xl font-bold mb-4">Recent Notifications</h2>
              <div className="space-y-3">
                {notifications.map((notification) => (
                  <div
                    key={notification.id}
                    className={`p-4 rounded-lg border transition-all hover:shadow-md ${
                      notification.read
                        ? 'bg-white border-gray-200'
                        : 'bg-blue-50 border-blue-200'
                    }`}
                  >
                    <div className="flex items-start space-x-3">
                      <div className="mt-1">{getIcon(notification.type)}</div>
                      <div className="flex-1">
                        <div className="flex items-start justify-between">
                          <h3 className="font-semibold text-gray-dark">
                            {notification.title}
                            {!notification.read && (
                              <span className="ml-2 w-2 h-2 bg-primary rounded-full inline-block"></span>
                            )}
                          </h3>
                          <span className="text-xs text-gray-500">{notification.date}</span>
                        </div>
                        <p className="text-sm text-gray-600 mt-1">{notification.message}</p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Notification Preferences */}
          <div className="space-y-4">
            <div className="card">
              <h2 className="text-xl font-bold mb-4">Preferences</h2>
              <div className="space-y-4">
                <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                  <div className="flex items-center space-x-3">
                    <Mail className="w-5 h-5 text-gray-600" />
                    <div>
                      <p className="font-medium">Email</p>
                      <p className="text-xs text-gray-500">Receive email notifications</p>
                    </div>
                  </div>
                  <label className="relative inline-flex items-center cursor-pointer">
                    <input
                      type="checkbox"
                      checked={preferences.email}
                      onChange={() =>
                        setPreferences({ ...preferences, email: !preferences.email })
                      }
                      className="sr-only peer"
                    />
                    <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-primary rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
                  </label>
                </div>

                <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                  <div className="flex items-center space-x-3">
                    <Smartphone className="w-5 h-5 text-gray-600" />
                    <div>
                      <p className="font-medium">SMS</p>
                      <p className="text-xs text-gray-500">Receive SMS notifications</p>
                    </div>
                  </div>
                  <label className="relative inline-flex items-center cursor-pointer">
                    <input
                      type="checkbox"
                      checked={preferences.sms}
                      onChange={() =>
                        setPreferences({ ...preferences, sms: !preferences.sms })
                      }
                      className="sr-only peer"
                    />
                    <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-primary rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
                  </label>
                </div>

                <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                  <div className="flex items-center space-x-3">
                    <Bell className="w-5 h-5 text-gray-600" />
                    <div>
                      <p className="font-medium">Push</p>
                      <p className="text-xs text-gray-500">Receive push notifications</p>
                    </div>
                  </div>
                  <label className="relative inline-flex items-center cursor-pointer">
                    <input
                      type="checkbox"
                      checked={preferences.push}
                      onChange={() =>
                        setPreferences({ ...preferences, push: !preferences.push })
                      }
                      className="sr-only peer"
                    />
                    <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-primary rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
                  </label>
                </div>
              </div>
            </div>

            <div className="card bg-gradient-to-br from-primary/10 to-blue-50">
              <div className="text-center">
                <Bell className="w-12 h-12 text-primary mx-auto mb-3" />
                <h3 className="font-bold mb-2">Stay Updated</h3>
                <p className="text-sm text-gray-600">
                  Get real-time notifications about your projects, tasks, and team activities.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </DashboardLayout>
  )
}
