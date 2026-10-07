import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ReportResponse } from '../../shared/models';
import { ReportApiService } from '../../core/services/report/report-api';
import { CaseApiService } from '../../core/services/case/case-api';
import { HearingApiService } from '../../core/services/hearing/hearing-api';
import { JudgmentApiService } from '../../core/services/judgment/judgment-api';
import { ComplianceAuditService } from '../../core/services/compliance/compliance-api';
import { AuthService } from '../../core/services/auth/auth';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './reports.html',
  styleUrls: ['./reports.css']
})
export class Reports implements OnInit {
  reportForm: FormGroup;
  
  reports = signal<ReportResponse[]>([]);
  loading = signal(true);
  generating = signal(false);
  
  generated = signal<ReportResponse | null>(null);
  parsedMetrics = signal<Record<string, any> | null>(null);
  selectedReport = signal<ReportResponse | null>(null);

  metricEntries = computed(() => {
    const metrics = this.parsedMetrics();
    if (!metrics) return [];
    return Object.entries(metrics).map(([key, value]) => ({ key, value }));
  });

  constructor(
    private fb: FormBuilder,
    private reportApi: ReportApiService,
    private caseApi: CaseApiService,
    private hearingApi: HearingApiService,
    private judgmentApi: JudgmentApiService,
    private complianceApi: ComplianceAuditService,
    private auth: AuthService
  ) {
    this.reportForm = this.fb.group({
      scope: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.reportApi.getAllReports().subscribe({
      next: (reportData) => { 
        // Force cast through unknown to resolve DOM Report interface clash
        this.reports.set(reportData as unknown as ReportResponse[]); 
        this.loading.set(false); 
      }, 
      error: () => this.loading.set(false) 
    });
  }

  reportMetricEntries(report: ReportResponse | null): { key: string, value: any }[] {
    if (!report) return [];
    try {
      const metrics = JSON.parse(report.metrics);
      return Object.entries(metrics).map(([key, value]) => ({ key, value }));
    } catch {
      return [];
    }
  }

  formatMetricKey(key: string): string {
    return key
      .split('_')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  getMetricValue(key: string): any {
    const report = this.selectedReport();
    if (!report) return null;
    try {
      const metrics = JSON.parse(report.metrics);
      return metrics[key] ?? 'N/A';
    } catch {
      return 'N/A';
    }
  }

  viewReport(report: ReportResponse): void {
    this.selectedReport.set(report);
  }

  backToList(): void {
    this.selectedReport.set(null);
  }

  generate(): void {
    if (this.reportForm.invalid) {
      this.reportForm.markAllAsTouched();
      return;
    }

    this.generating.set(true);
    const currentScope = this.reportForm.value.scope;
    const userId = this.auth.currentUser?.userId ?? 1;

    if (currentScope === 'CASE') {
      this.caseApi.getAll().pipe(catchError(() => of([]))).subscribe(cases => {
        const metrics = {
          total_cases: cases.length,
          active_cases: cases.filter(caseItem => caseItem.status === 'ACTIVE').length,
          closed_cases: cases.filter(caseItem => caseItem.status === 'CLOSED').length,
          under_review: cases.filter(caseItem => caseItem.status === 'UNDER_REVIEW').length,
          pending_judgment: cases.filter(caseItem => caseItem.status === 'JUDGMENT_PENDING').length,
          avg_duration_days: cases.length > 0 
            ? Math.round(cases.reduce((sum, caseItem) => {
                const filedTime = new Date(caseItem.filedDate).getTime();
                const nowTime = new Date().getTime();
                return sum + (nowTime - filedTime) / (1000 * 60 * 60 * 24);
              }, 0) / cases.length)
            : 0
        };
        this.saveReport(userId, currentScope, metrics);
      });

    } else if (currentScope === 'HEARING') {
      this.hearingApi.getAllHearings().pipe(catchError(() => of([]))).subscribe(hearings => {
        const metrics = {
          total_hearings: hearings.length,
          scheduled: hearings.filter(hearingItem => hearingItem.status === 'SCHEDULED').length,
          completed: hearings.filter(hearingItem => hearingItem.status === 'COMPLETED').length,
          adjourned: hearings.filter(hearingItem => hearingItem.status === 'ADJOURNED').length,
          cancelled: hearings.filter(hearingItem => hearingItem.status === 'CANCELLED').length
        };
        this.saveReport(userId, currentScope, metrics);
      });

    } else if (currentScope === 'JUDGMENT') {
      this.judgmentApi.getAll().pipe(catchError(() => of([]))).subscribe(judgments => {
        const metrics = {
          total_judgments: judgments.length,
          finalized: judgments.filter(judgmentItem => judgmentItem.status === 'FINAL').length,
          draft: judgments.filter(judgmentItem => judgmentItem.status === 'DRAFT').length,
          pending: judgments.filter(judgmentItem => judgmentItem.status !== 'FINAL' && judgmentItem.status !== 'DRAFT').length
        };
        this.saveReport(userId, currentScope, metrics);
      });

    } else if (currentScope === 'COMPLIANCE') {
      this.complianceApi.getAllComplianceRecords().pipe(catchError(() => of([]))).subscribe(records => {
        const metrics = {
          total_records: records.length,
          compliant: records.filter(recordItem => recordItem.result === 'COMPLIANT').length,
          non_compliant: records.filter(recordItem => recordItem.result === 'NON_COMPLIANT').length,
          pending_review: records.filter(recordItem => recordItem.result === 'PENDING').length,
          compliance_rate: records.length > 0 
            ? Math.round((records.filter(recordItem => recordItem.result === 'COMPLIANT').length / records.length) * 100)
            : 0
        };
        this.saveReport(userId, currentScope, metrics);
      });

    } else {
      this.generating.set(false);
    }
  }

  private saveReport(userId: number, scopeStr: string, metrics: Record<string, any>): void {
    const payload = { 
      scope: scopeStr, 
      generatedBy: userId,
      metrics: JSON.stringify(metrics)
    };

    this.reportApi.generateReport(payload as any).subscribe({
      next: (reportResult) => {
        // Cast through unknown to resolve DOM interface clash
        const newReport = reportResult as unknown as ReportResponse;
        this.generated.set(newReport);
        
        try { 
          this.parsedMetrics.set(JSON.parse(newReport.metrics)); 
        } catch { 
          this.parsedMetrics.set(metrics); 
        }
        
        this.reports.update(r => [newReport, ...r]);
        this.generating.set(false);
      },
      error: () => this.generating.set(false)
    });
  }
}