import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CaseRequest, CaseResponse, DocumentRequest, DocumentResponse } from '../../../shared/models';

const API = `http://localhost:8081/api/cases`;

@Injectable({ providedIn: 'root' })
export class CaseApiService {
  constructor(private http: HttpClient) {}

  // POST /api/cases/file
  fileNewCase(req: CaseRequest): Observable<CaseResponse> {
    return this.http.post<CaseResponse>(`${API}/file`, req);
  }

  // GET /api/cases/{id}
  getById(id: number | string): Observable<CaseResponse> {
    return this.http.get<CaseResponse>(`${API}/${id}`);
  }

  // GET /api/cases
  getAll(): Observable<CaseResponse[]> {
    return this.http.get<CaseResponse[]>(`${API}`);
  }

  // GET /api/cases/citizen/{citizenId}
  getByCitizenId(citizenId: number | string): Observable<CaseResponse[]> {
    return this.http.get<CaseResponse[]>(`${API}/citizen/${citizenId}`);
  }

  // GET /api/cases/lawyer/{lawyerId}
  getByLawyerId(lawyerId: number | string): Observable<CaseResponse[]> {
    return this.http.get<CaseResponse[]>(`${API}/lawyer/${lawyerId}`);
  }

  // GET /api/cases/judge/{judgeId}
  getByJudgeId(judgeId: number | string): Observable<CaseResponse[]> {
    return this.http.get<CaseResponse[]>(`${API}/judge/${judgeId}`);
  }

  // GET /api/cases/status/{status}
  getByStatus(status: string): Observable<CaseResponse[]> {
    return this.http.get<CaseResponse[]>(`${API}/status/${status}`);
  }

  // PATCH /api/cases/{id}/status?status=...
  updateStatus(id: number | string, status: string): Observable<CaseResponse> {
    const params = new HttpParams().set('status', status);
    return this.http.patch<CaseResponse>(`${API}/${id}/status`, null, { params });
  }

  // PATCH /api/cases/{id}/assign-lawyer?lawyerId=...
  assignLawyer(id: number | string, lawyerId: number | string): Observable<CaseResponse> {
    const params = new HttpParams().set('lawyerId', lawyerId.toString());
    return this.http.patch<CaseResponse>(`${API}/${id}/assign-lawyer`, null, { params });
  }

  
// ✅ ✅ NEW → REMOVE LAWYER (MISSING FIXED)
  removeLawyer(id: number | string): Observable<CaseResponse> {
    return this.http.patch<CaseResponse>(`${API}/${id}/remove-lawyer`, null);
  }


  // PATCH /api/cases/{id}/assign-judge?judgeId=...
  assignJudge(id: number | string, judgeId: number | string): Observable<CaseResponse> {
    const params = new HttpParams().set('judgeId', judgeId.toString());
    return this.http.patch<CaseResponse>(`${API}/${id}/assign-judge`, null, { params });
  }

  // POST /api/cases/{caseId}/documents/upload
  uploadDocument(caseId: number | string, req: DocumentRequest): Observable<DocumentResponse> {
    return this.http.post<DocumentResponse>(`${API}/${caseId}/documents/upload`, req);
  }

  // GET /api/cases/{caseId}/documents
  getDocuments(caseId: number | string): Observable<DocumentResponse[]> {
    return this.http.get<DocumentResponse[]>(`${API}/${caseId}/documents`);
  }

  // PATCH /api/cases/documents/{docId}/verify?status=...
  verifyDocument(docId: number | string, status: string): Observable<DocumentResponse> {
    const params = new HttpParams().set('status', status);
    return this.http.patch<DocumentResponse>(`${API}/documents/${docId}/verify`, null, { params });
  }

  

}