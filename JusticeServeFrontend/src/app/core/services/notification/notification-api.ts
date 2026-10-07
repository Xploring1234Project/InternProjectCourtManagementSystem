import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { NotificationRequest, NotificationResponse } from '../../../shared/models';

@Injectable({ providedIn: 'root' })
export class NotificationApiService {
  private readonly API = `http://localhost:8081/api/notifications`;

  constructor(private http: HttpClient) {}

  
   // Fetch all notifications for a specific user
  getUserNotifications(userId: number | string): Observable<NotificationResponse[]> {
    return this.http.get<NotificationResponse[]>(`${this.API}/user/${userId}`);
  }

  
  //Get the count of UNREAD notifications (useful for badges)
  getUnreadCount(userId: number | string): Observable<number> {
    return this.http.get<number>(`${this.API}/user/${userId}/unread-count`);
  }

  
  // Mark a single notification as READ
  markAsRead(id: number | string): Observable<NotificationResponse> {
    return this.http.patch<NotificationResponse>(`${this.API}/${id}/read`, {});
  }


  // Mark all notifications for a user as READ at once
  markAllAsRead(userId: number | string): Observable<void> {
    return this.http.patch<void>(`${this.API}/user/${userId}/read-all`, {});
  }

  
  // Send a manual/new notification
  sendNotification(req: NotificationRequest): Observable<NotificationResponse> {
    return this.http.post<NotificationResponse>(this.API, req);
  }

  // Internal send endpoint (if needed by frontend, though usually for service-to-service)
  sendInternal(req: NotificationRequest): Observable<NotificationResponse> {
    return this.http.post<NotificationResponse>(`${this.API}/internal`, req);
  }
}