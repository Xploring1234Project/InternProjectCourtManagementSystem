import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CourtOrderRequest, CourtOrderResponse } from '../../../shared/models';

const API = `http://localhost:8081/api/court-orders`;

@Injectable({ providedIn: 'root' })
export class CourtOrderApiService {
  constructor(private http: HttpClient) {}

  // POST /api/court-orders
  issueOrder(req: CourtOrderRequest): Observable<CourtOrderResponse> {
    return this.http.post<CourtOrderResponse>(`${API}`, req);
  }

  // GET /api/court-orders
  getAll(): Observable<CourtOrderResponse[]> {
    return this.http.get<CourtOrderResponse[]>(`${API}`);
  }

  // GET /api/court-orders/{id}
  getById(id: number | string): Observable<CourtOrderResponse> {
    return this.http.get<CourtOrderResponse>(`${API}/${id}`);
  }

  // GET /api/court-orders/case/{caseId}
  getByCaseId(caseId: number | string): Observable<CourtOrderResponse[]> {
    return this.http.get<CourtOrderResponse[]>(`${API}/case/${caseId}`);
  }

  // PATCH /api/court-orders/{id}/status?status=...
  updateStatus(id: number | string, status: string): Observable<CourtOrderResponse> {
    const params = new HttpParams().set('status', status);
    return this.http.patch<CourtOrderResponse>(`${API}/${id}/status`, null, { params });
  }
}