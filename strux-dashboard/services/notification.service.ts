import apiClient from '@/lib/api/client'
import { Notification, NotificationPreferences } from '@/lib/types'

class NotificationService {
  async getUserNotifications(userId: string): Promise<Notification[]> {
    return apiClient.get<Notification[]>(`/api/notifications/user/${userId}`)
  }

  async getUnreadNotifications(userId: string): Promise<Notification[]> {
    return apiClient.get<Notification[]>(`/api/notifications/user/${userId}/unread`)
  }

  async getUnreadCount(userId: string): Promise<{ count: number }> {
    return apiClient.get<{ count: number }>(`/api/notifications/user/${userId}/unread-count`)
  }

  async markAsRead(notificationId: string): Promise<void> {
    return apiClient.put(`/api/notifications/${notificationId}/read`)
  }

  async markAllAsRead(userId: string): Promise<void> {
    return apiClient.put(`/api/notifications/user/${userId}/read-all`)
  }

  async deleteNotification(notificationId: string): Promise<void> {
    return apiClient.delete(`/api/notifications/${notificationId}`)
  }

  async getPreferences(userId: string): Promise<NotificationPreferences> {
    return apiClient.get<NotificationPreferences>(`/api/notifications/preferences/${userId}`)
  }

  async updatePreferences(
    userId: string,
    preferences: Partial<NotificationPreferences>
  ): Promise<NotificationPreferences> {
    return apiClient.put<NotificationPreferences>(
      `/api/notifications/preferences/${userId}`,
      preferences
    )
  }
}

export const notificationService = new NotificationService()
export default notificationService
