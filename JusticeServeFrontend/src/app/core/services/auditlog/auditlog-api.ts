import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuditLogResponse } from '../../../shared/models';

@Injectable({ providedIn: 'root' })
export class AuditLogApiService {
  private readonly API = `http://localhost:8081/api/audit-logs`;

  constructor(private http: HttpClient) {}

  /**
   * Internal logging method.
   * Sends userId, action, and resource as Query Parameters.
   */
  logInternal(userId: number, action: string, resource: string): Observable<void> {
    const params = new HttpParams()
      .set('userId', userId.toString())
      .set('action', action)
      .set('resource', resource);

    // POST /api/audit-logs/internal?userId=...&action=...&resource=...
    return this.http.post<void>(`${this.API}/internal`, null, { params });
  }

  /**
   * Fetches the entire system audit trail.
   * Restricted to ADMIN and AUDITOR roles.
   */
  getSystemAuditTrail(): Observable<AuditLogResponse[]> {
    return this.http.get<AuditLogResponse[]>(`${this.API}/all`);
  }

  /**
   * Fetches the audit history for a specific user ID.
   */
  getUserAuditHistory(userId: number | string): Observable<AuditLogResponse[]> {
    return this.http.get<AuditLogResponse[]>(`${this.API}/user/${userId}`);
  }
}