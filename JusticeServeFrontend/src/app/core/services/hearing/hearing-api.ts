import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HearingRequest, HearingResponse, ProceedingRequest, ProceedingResponse } from '../../../shared/models';

const API = `http://localhost:8081/api/hearings`;

@Injectable({ providedIn: 'root' })
export class HearingApiService {
  constructor(private http: HttpClient) {}

  // POST /api/hearings
  scheduleHearing(req: HearingRequest): Observable<HearingResponse> {
    return this.http.post<HearingResponse>(`${API}`, req);
  }

  // GET /api/hearings
  getAllHearings(): Observable<HearingResponse[]> {
    return this.http.get<HearingResponse[]>(`${API}`);
  }

  // GET /api/hearings/{id}
  getHearingById(id: number | string): Observable<HearingResponse> {
    return this.http.get<HearingResponse>(`${API}/${id}`);
  }

  // GET /api/hearings/case/{caseId}
  getHearingsByCase(caseId: number | string): Observable<HearingResponse[]> {
    return this.http.get<HearingResponse[]>(`${API}/case/${caseId}`);
  }

  // GET /api/hearings/judge/{judgeId}
  getHearingsByJudge(judgeId: number | string): Observable<HearingResponse[]> {
    return this.http.get<HearingResponse[]>(`${API}/judge/${judgeId}`);
  }

  // PATCH /api/hearings/{id}/status?status=...
  updateStatus(id: number | string, status: string): Observable<HearingResponse> {
    const params = new HttpParams().set('status', status);
    return this.http.patch<HearingResponse>(`${API}/${id}/status`, null, { params });
  }

  // POST /api/hearings/{id}/proceedings
  addProceeding(hearingId: number | string, req: ProceedingRequest): Observable<ProceedingResponse> {
    return this.http.post<ProceedingResponse>(`${API}/${hearingId}/proceedings`, req);
  }

  // GET /api/hearings/{id}/proceedings
  getProceedings(hearingId: number | string): Observable<ProceedingResponse[]> {
    return this.http.get<ProceedingResponse[]>(`${API}/${hearingId}/proceedings`);
  }
}