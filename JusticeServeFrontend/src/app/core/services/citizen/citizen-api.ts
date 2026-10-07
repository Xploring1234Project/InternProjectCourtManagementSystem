import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CitizenRequest, CitizenResponse, DocumentRequest, DocumentResponse } from '../../../shared/models';

const API = `http://localhost:8081/api/citizens`;

@Injectable({ providedIn: 'root' })
export class CitizenApiService {
  constructor(private http: HttpClient) {}

  create(req: CitizenRequest): Observable<CitizenResponse> {
    return this.http.post<CitizenResponse>(`${API}`, req);
  }

  getAll(): Observable<CitizenResponse[]> {
    return this.http.get<CitizenResponse[]>(`${API}`);
  }

  getById(id: number | string): Observable<CitizenResponse> {
    return this.http.get<CitizenResponse>(`${API}/${id}`);
  }

  getByUserId(userId: number | string): Observable<CitizenResponse> {
    return this.http.get<CitizenResponse>(`${API}/user/${userId}`);
  }

  update(id: number | string, req: CitizenRequest): Observable<CitizenResponse> {
    return this.http.put<CitizenResponse>(`${API}/${id}`, req);
  }

  addDocument(citizenId: number | string, req: DocumentRequest): Observable<DocumentResponse> {
    return this.http.post<DocumentResponse>(`${API}/${citizenId}/documents`, req);
  }

  getDocuments(citizenId: number | string): Observable<DocumentResponse[]> {
    return this.http.get<DocumentResponse[]>(`${API}/${citizenId}/documents`);
  }

  verifyDocument(docId: number | string, status: string): Observable<DocumentResponse> {
    return this.http.patch<DocumentResponse>(
      `${API}/documents/${docId}/verify`, 
      null, // No request body
      { params: { status } } // Status is passed as a query param (?status=...)
    );
  }
}