import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserResponse } from '../../../shared/models';

const API = `http://localhost:8081/api/users`;

@Injectable({ providedIn: 'root' })
export class UserApiService {
  constructor(private http: HttpClient) {}

  getAll(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${API}`);
  }

  getById(id: number): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${API}/${id}`);
  }

  getByRole(role: string): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${API}/role/${role.toUpperCase()}`);
  }

  updateStatus(id: number, status: string): Observable<UserResponse> {
    return this.http.patch<UserResponse>(
      `${API}/${id}/status`,
      null,
      { params: { status } }
    );
  }

  updateRole(id: number, role: string): Observable<UserResponse> {
    return this.http.patch<UserResponse>(
      `${API}/${id}/change-role`,
      { role }
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/${id}`);
  }
}