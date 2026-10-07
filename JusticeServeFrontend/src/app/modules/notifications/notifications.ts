import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { NotificationResponse } from '../../shared/models';
import { NotificationApiService } from '../../core/services/notification/notification-api';
import { AuthService } from '../../core/services/auth/auth';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './notifications.html',
  styleUrls: ['./notifications.css']
})
export class Notifications implements OnInit {
  notifications = signal<NotificationResponse[]>([]);
  loading = signal(true);
  selectedNotification = signal<NotificationResponse | null>(null);
  notificationDetailLines = signal<string[]>([]);

  unreadCount = computed(() => this.notifications().filter(notification => notification.status === 'UNREAD').length);
  unread = computed(() => this.notifications().filter(notification => notification.status === 'UNREAD'));
  read = computed(() => this.notifications().filter(notification => notification.status !== 'UNREAD'));

  constructor(
    private notifApi: NotificationApiService, 
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    const userId = this.auth.currentUser?.userId;

    if (userId) {
      this.notifApi.getUserNotifications(userId).subscribe({
        next: (notificationsData) => {
          this.notifications.set(notificationsData.sort((a, b) =>
            new Date(b.createdDate).getTime() - new Date(a.createdDate).getTime()
          ));
          this.loading.set(false);
        },
        error: () => this.loading.set(false)
      });
    } else {
      this.loading.set(false);
    }
  }

  markRead(notificationItem: NotificationResponse): void {
    this.notifApi.markAsRead(notificationItem.notificationId).subscribe(updatedNotification => {
      this.notifications.update(n => n.map(notification =>
        notification.notificationId === notificationItem.notificationId ? updatedNotification : notification
      ));
    });
  }

  markAllRead(): void {
    const userId = this.auth.currentUser?.userId;
    if (!userId) return;

    this.notifApi.markAllAsRead(userId).subscribe({
      next: () => {
        this.notifications.update(n => n.map(notification => ({ ...notification, status: 'READ' })));
      },
      error: () => {
        // Fallback: mark each unread one individually
        this.unread().forEach(notification => this.markRead(notification));
      }
    });
  }

  getCategoryTitle(category: string): string {
    const titleMap: Record<string, string> = {
      CASE: '📁 Case Update',
      DOCUMENT_REQUEST: '📄 Document Request',
      HEARING: '📅 Hearing Notice',
      JUDGMENT: '⚖️ Judgment Update',
      COMPLIANCE: '🛡️ Compliance Alert'
    };
    return titleMap[category] ?? '🔔 Notification';
  }

  getCategoryColor(category: string): string {
    const colorMap: Record<string, string> = {
      CASE: '#0d6efd', 
      DOCUMENT_REQUEST: '#fd7e14',
      HEARING: '#0dcaf0', 
      JUDGMENT: '#ffc107', 
      COMPLIANCE: '#198754'
    };
    return colorMap[category] ?? '#6c757d';
  }

  getCategoryBackground(category: string): string {
    const backgroundMap: Record<string, string> = {
      CASE: 'bg-primary text-white',
      DOCUMENT_REQUEST: 'bg-warning text-dark',
      HEARING: 'bg-info text-dark',
      JUDGMENT: 'bg-warning text-dark',
      COMPLIANCE: 'bg-success text-white'
    };
    return backgroundMap[category] ?? 'bg-secondary text-white';
  }

  getCategoryIcon(category: string): string {
    const iconMap: Record<string, string> = {
      CASE: 'bi-folder2-open',
      DOCUMENT_REQUEST: 'bi-file-earmark-text',
      HEARING: 'bi-calendar-event',
      JUDGMENT: 'bi-hammer',
      COMPLIANCE: 'bi-shield-check'
    };
    return iconMap[category] ?? 'bi-bell';
  }

  isDocumentRequest(notification: NotificationResponse): boolean {
    const messageContent = notification.message.toLowerCase();
    return messageContent.includes('document request') || messageContent.includes('reply to document request');
  }

  isBoldLine(line: string): boolean {
    return line.startsWith('Case Title:') || 
           line.startsWith('Case Description:') || 
           line.startsWith('Requested Document Type:') || 
           line.startsWith('Subject:') || 
           line.startsWith('Document Request') || 
           line.startsWith('Reply');
  }

  viewMessage(notification: NotificationResponse): void {
    this.selectedNotification.set(notification);
    this.notificationDetailLines.set(notification.message?.split('\n') || []);
    this.markRead(notification);
  }

  closeMessage(): void {
    this.selectedNotification.set(null);
    this.notificationDetailLines.set([]);
  }
}