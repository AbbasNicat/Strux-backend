'use client'

import { useEffect } from 'react'
import Link from 'next/link'
import { Building2, CheckCircle } from 'lucide-react'
import { useAuth } from '@/contexts/AuthContext'

export default function LogoutPage() {
  const { logout } = useAuth()

  useEffect(() => {
    // Auto logout when this page is visited
    logout()
  }, [])

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-light">
      <div className="card max-w-md w-full mx-4 text-center">
        <div className="flex justify-center mb-6">
          <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center">
            <CheckCircle className="w-12 h-12 text-green-600" />
          </div>
        </div>

        <div className="flex items-center justify-center mb-4">
          <Building2 className="w-8 h-8 text-primary" />
        </div>

        <h1 className="text-3xl font-bold text-gray-dark mb-4">
          Logged Out Successfully
        </h1>

        <p className="text-gray-600 mb-8">
          You have been logged out of your Strux account. Thank you for using our platform!
        </p>

        <Link href="/login">
          <button className="btn-primary w-full mb-4">
            Login Again
          </button>
        </Link>

        <Link href="/">
          <button className="btn-secondary w-full">
            Back to Home
          </button>
        </Link>
      </div>
    </div>
  )
}
