import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ReportResponse, ReportScope } from '../../../shared/models';

@Injectable({ providedIn: 'root' })
export class ReportApiService {
  private readonly API = `http://localhost:8081/api/reports`;

  constructor(private http: HttpClient) {}

 
   // Generates a new report. 
   // @param criteria - A flexible object containing report parameters (scope, date ranges, etc.)
  generateReport(criteria: any): Observable<Report> {
    return this.http.post<Report>(`${this.API}/generate`, criteria);
  }


  // Fetches all generated reports from the system
  getAllReports(): Observable<Report[]> {
    return this.http.get<Report[]>(this.API);
  }


   //Fetches reports filtered by a specific scope (e.g., CASE, HEARING)
  getReportsByScope(scope: ReportScope): Observable<Report[]> {
    return this.http.get<Report[]>(`${this.API}/scope/${scope}`);
  }

  // Fetches details of a specific report by its ID
  getReportById(id: number | string): Observable<Report> {
    return this.http.get<Report>(`${this.API}/${id}`);
  }
}