'use client'

import DashboardLayout from '@/components/DashboardLayout'
import { Shield, User, Bell, Lock } from 'lucide-react'
import { useState } from 'react'

export default function SettingsPage() {
  const [activeTab, setActiveTab] = useState('account')
  const [twoFactorEnabled, setTwoFactorEnabled] = useState(false)

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-dark">Settings</h1>
          <p className="text-gray-600 mt-1">Manage your account settings and preferences</p>
        </div>

        {/* Tabs */}
        <div className="border-b border-gray-200">
          <div className="flex space-x-8">
            <button
              onClick={() => setActiveTab('account')}
              className={`pb-4 px-2 border-b-2 transition-all ${
                activeTab === 'account'
                  ? 'border-primary text-primary font-semibold'
                  : 'border-transparent text-gray-600 hover:text-gray-900'
              }`}
            >
              <div className="flex items-center space-x-2">
                <User className="w-5 h-5" />
                <span>Account</span>
              </div>
            </button>
            <button
              onClick={() => setActiveTab('security')}
              className={`pb-4 px-2 border-b-2 transition-all ${
                activeTab === 'security'
                  ? 'border-primary text-primary font-semibold'
                  : 'border-transparent text-gray-600 hover:text-gray-900'
              }`}
            >
              <div className="flex items-center space-x-2">
                <Shield className="w-5 h-5" />
                <span>Security</span>
              </div>
            </button>
            <button
              onClick={() => setActiveTab('notifications')}
              className={`pb-4 px-2 border-b-2 transition-all ${
                activeTab === 'notifications'
                  ? 'border-primary text-primary font-semibold'
                  : 'border-transparent text-gray-600 hover:text-gray-900'
              }`}
            >
              <div className="flex items-center space-x-2">
                <Bell className="w-5 h-5" />
                <span>Notifications</span>
              </div>
            </button>
          </div>
        </div>

        {/* Account Tab */}
        {activeTab === 'account' && (
          <div className="card">
            <h2 className="text-xl font-bold mb-6">Account Settings</h2>
            <form className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Username
                </label>
                <input
                  type="text"
                  defaultValue="admin_user"
                  className="input"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Email
                </label>
                <input
                  type="email"
                  defaultValue="admin@strux.az"
                  className="input"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Language
                </label>
                <select className="input">
                  <option>English</option>
                  <option>Azerbaijani</option>
                  <option>Russian</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Timezone
                </label>
                <select className="input">
                  <option>Asia/Baku (GMT+4)</option>
                  <option>UTC</option>
                  <option>Europe/Istanbul</option>
                </select>
              </div>

              <button type="submit" className="btn-primary">
                Save Changes
              </button>
            </form>
          </div>
        )}

        {/* Security Tab */}
        {activeTab === 'security' && (
          <div className="space-y-6">
            <div className="card">
              <h2 className="text-xl font-bold mb-6">Two-Factor Authentication</h2>
              <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                <div className="flex items-center space-x-3">
                  <Lock className="w-6 h-6 text-gray-600" />
                  <div>
                    <p className="font-medium">Enable 2FA</p>
                    <p className="text-sm text-gray-600">
                      Add an extra layer of security to your account
                    </p>
                  </div>
                </div>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={twoFactorEnabled}
                    onChange={() => setTwoFactorEnabled(!twoFactorEnabled)}
                    className="sr-only peer"
                  />
                  <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-primary rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
                </label>
              </div>
            </div>

            <div className="card">
              <h2 className="text-xl font-bold mb-6">Change Password</h2>
              <form className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Current Password
                  </label>
                  <input
                    type="password"
                    placeholder="Enter current password"
                    className="input"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    New Password
                  </label>
                  <input
                    type="password"
                    placeholder="Enter new password"
                    className="input"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Confirm New Password
                  </label>
                  <input
                    type="password"
                    placeholder="Confirm new password"
                    className="input"
                  />
                </div>

                <button type="submit" className="btn-primary">
                  Update Password
                </button>
              </form>
            </div>
          </div>
        )}

        {/* Notifications Tab */}
        {activeTab === 'notifications' && (
          <div className="card">
            <h2 className="text-xl font-bold mb-6">Notification Preferences</h2>
            <div className="space-y-4">
              <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                <div>
                  <p className="font-medium">Project Updates</p>
                  <p className="text-sm text-gray-600">
                    Get notified about project status changes
                  </p>
                </div>
                <input type="checkbox" defaultChecked className="w-5 h-5 text-primary" />
              </div>

              <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                <div>
                  <p className="font-medium">Task Assignments</p>
                  <p className="text-sm text-gray-600">
                    Get notified when tasks are assigned to you
                  </p>
                </div>
                <input type="checkbox" defaultChecked className="w-5 h-5 text-primary" />
              </div>

              <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                <div>
                  <p className="font-medium">Worker Updates</p>
                  <p className="text-sm text-gray-600">
                    Get notified about worker availability changes
                  </p>
                </div>
                <input type="checkbox" className="w-5 h-5 text-primary" />
              </div>

              <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                <div>
                  <p className="font-medium">System Updates</p>
                  <p className="text-sm text-gray-600">
                    Get notified about platform updates and maintenance
                  </p>
                </div>
                <input type="checkbox" defaultChecked className="w-5 h-5 text-primary" />
              </div>
            </div>

            <button className="btn-primary mt-6">
              Save Preferences
            </button>
          </div>
        )}
      </div>
    </DashboardLayout>
  )
}
