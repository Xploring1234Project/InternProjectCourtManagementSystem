import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { 
  ComplianceRecordRequest, ComplianceRecordResponse, 
  AuditRequest, AuditResponse 
} from '../../../shared/models';

@Injectable({ providedIn: 'root' })
export class ComplianceAuditService {
  private readonly COMPLIANCE_API = `http://localhost:8081/api/compliance`;
  private readonly AUDIT_API = `http://localhost:8081/api/audits`;

  constructor(private http: HttpClient) {}

  // ==========================================
  //         COMPLIANCE RECORD METHODS
  // ==========================================

  createComplianceRecord(req: ComplianceRecordRequest): Observable<ComplianceRecordResponse> {
    return this.http.post<ComplianceRecordResponse>(this.COMPLIANCE_API, req);
  }

  getAllComplianceRecords(): Observable<ComplianceRecordResponse[]> {
    return this.http.get<ComplianceRecordResponse[]>(this.COMPLIANCE_API);
  }

  getComplianceRecordById(id: number | string): Observable<ComplianceRecordResponse> {
    return this.http.get<ComplianceRecordResponse>(`${this.COMPLIANCE_API}/${id}`);
  }

  updateComplianceRecord(id: number | string, req: ComplianceRecordRequest): Observable<ComplianceRecordResponse> {
    return this.http.patch<ComplianceRecordResponse>(`${this.COMPLIANCE_API}/${id}`, req);
  }

  // ==========================================
  //            AUDIT METHODS
  // ==========================================

  initiateAudit(req: AuditRequest): Observable<AuditResponse> {
    return this.http.post<AuditResponse>(this.AUDIT_API, req);
  }

  getAllAudits(): Observable<AuditResponse[]> {
    return this.http.get<AuditResponse[]>(this.AUDIT_API);
  }

  getAuditById(id: number | string): Observable<AuditResponse> {
    return this.http.get<AuditResponse>(`${this.AUDIT_API}/${id}`);
  }

  updateAuditStatus(id: number | string, status: string): Observable<AuditResponse> {
    // PATCH /api/audits/{id}/status?status=...
    const params = new HttpParams().set('status', status);
    return this.http.patch<AuditResponse>(`${this.AUDIT_API}/${id}/status`, null, { params });
  }
}