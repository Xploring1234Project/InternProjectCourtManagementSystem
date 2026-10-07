import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JudgmentRequest, JudgmentResponse } from '../../../shared/models';

const API = `http://localhost:8081/api/judgments`;

@Injectable({ providedIn: 'root' })
export class JudgmentApiService {
  constructor(private http: HttpClient) {}

  // POST /api/judgments
  recordJudgment(req: JudgmentRequest): Observable<JudgmentResponse> {
    return this.http.post<JudgmentResponse>(`${API}`, req);
  }

  // GET /api/judgments
  getAll(): Observable<JudgmentResponse[]> {
    return this.http.get<JudgmentResponse[]>(`${API}`);
  }

  // GET /api/judgments/{id}
  getById(id: number | string): Observable<JudgmentResponse> {
    return this.http.get<JudgmentResponse>(`${API}/${id}`);
  }

  // GET /api/judgments/case/{caseId}
  getByCaseId(caseId: number | string): Observable<JudgmentResponse[]> {
    return this.http.get<JudgmentResponse[]>(`${API}/case/${caseId}`);
  }

  // PATCH /api/judgments/{id}/finalize
  finalize(id: number | string): Observable<JudgmentResponse> {
    return this.http.patch<JudgmentResponse>(`${API}/${id}/finalize`, null);
  }
}