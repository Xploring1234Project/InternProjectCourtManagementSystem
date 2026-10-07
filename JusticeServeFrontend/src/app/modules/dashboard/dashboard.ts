import { Component, OnInit, signal, computed } from '@angular/core';
import { CaseApiService } from '../../core/services/case/case-api';
import { HearingApiService } from '../../core/services/hearing/hearing-api';
import { JudgmentApiService } from '../../core/services/judgment/judgment-api';
import { CitizenApiService } from '../../core/services/citizen/citizen-api';
import { AuthService } from '../../core/services/auth/auth';
import { catchError, forkJoin, of } from 'rxjs';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {

  loading = signal<boolean>(true);
  statistics = signal<any>({});
  recentCases = signal<any[]>([]);
  recentHearings = signal<any[]>([]);


  constructor(private caseApi: CaseApiService,
              private hearingApi: HearingApiService,
              private judgmentApi: JudgmentApiService,
              private citizenApi: CitizenApiService,
              private authService: AuthService) { }

  userName = computed(() => this.authService.currentUser?.name ?? '');
  userRole = computed(() => this.authService.currentUser?.role ?? '');
  isAdmin = computed(() => this.authService.hasRole('ADMIN'));
  isJudge = computed(() => this.authService.hasRole('JUDGE'));
  isClerk = computed(() => this.authService.hasRole('CLERK'));
  isLawyer = computed(() => this.authService.hasRole('LAWYER'));
  isCitizen = computed(() => this.authService.hasRole('CITIZEN'));
  isCompliance = computed(() => this.authService.hasRole('COMPLIANCE'));
  isAuditor = computed(() => this.authService.hasRole('AUDITOR'));

  ngOnInit(): void {
    const userId = this.authService.currentUser?.userId;
    const role = this.userRole();

    if (role === "CITIZEN") {
      this.citizenApi.getByUserId(userId!).subscribe({
        next: citizen => {
          this.caseApi.getByCitizenId(citizen.citizenId).subscribe(cases => {
            this.statistics.update(s => ({
              ...s,
              myCases: cases.length,
              myActiveCases: cases.filter(c => c.status === 'ACTIVE').length,
              myClosedCases: cases.filter(c => c.status === 'CLOSED ').length
            }));
            this.recentCases.set(cases.slice(-5).reverse());
            this.loading.set(false);
          })
        },
        error: () => { this.statistics.set({}); this.loading.set(false); }
      })
    } else if (role === "JUDGE") {
      forkJoin({
        hearings: this.hearingApi.getHearingsByJudge(userId!).pipe(catchError(() => of([]))),
        judgments: this.judgmentApi.getAll().pipe(catchError(() => of([]))),
        cases: this.caseApi.getAll().pipe(catchError(() => of([])))
      }).subscribe(({ hearings, judgments, cases }) => {
        this.statistics.update(s => ({
          ...s,
          myHearings: hearings.length,
          pendingJudgments: judgments.filter(j => j.status === 'DRAFT').length,
          finalizedJudgments: judgments.filter(j => j.status === 'FINAL').length,
          totalCases: cases.length
        }));
        this.recentHearings.set(hearings.splice(-5).reverse());
        this.loading.set(false);
      })
    } else if (role === 'LAWYER') {
      this.caseApi.getByLawyerId(userId!).subscribe({
        next: cases => {
          this.statistics.update(s => ({
            ...s,
            assignedCases: cases.length,
            myActiveCases: cases.filter(c => c.status === 'ACTIVE').length
          }));
          this.recentCases.set(cases.slice(-5).reverse());

          //fetching Hearings for each case
          const allHearings: any[] = []
          let pending = cases.length

          if (pending === 0) {
            this.statistics.update(s => ({ ...s, totalHearings: 0 }));
            this.loading.set(false);
            return
          }
          cases.forEach(c => {
            this.hearingApi.getHearingsByCase(c.caseId).subscribe({
              next: caseHearings => {
                allHearings.push(...caseHearings)
                pending--;
                if (pending == 0) {
                  this.statistics.update(s => ({ ...s, totalHearings: allHearings.length }));
                  this.recentHearings.set(allHearings.slice(-5).reverse());
                  this.loading.set(false);
                }
              },
              error: () => {
                pending--;
                if (pending === 0) {
                  this.statistics.update(s => ({ ...s, totalHearings: allHearings.length }));
                  this.recentHearings.set(allHearings.slice(-5).reverse());
                  this.loading.set(false);
                }
              }
            })
          })
        }
      })
    } else {
      // ADMIN, CLERK, COMPLIANCE, AUDITOR
      forkJoin({
        cases: this.caseApi.getAll().pipe(catchError(() => of([]))),
        hearings: this.hearingApi.getAllHearings().pipe(catchError(() => of([]))),
        citizens: this.citizenApi.getAll().pipe(catchError(() => of([]))),
        judgments: this.judgmentApi.getAll().pipe(catchError(() => of([])))
      }).subscribe(({ cases, hearings, citizens, judgments }) => {
        this.statistics.update(s => ({
          ...s,
          totalCases: cases.length,
          activeCases: (cases as any[]).filter((c: any) => c.status === 'ACTIVE').length,
          underReview: (cases as any[]).filter((c: any) => c.status === 'UNDER_REVIEW').length,
          totalHearings: hearings.length,
          scheduledHearings: (hearings as any[]).filter((h: any) => h.status === 'SCHEDULED').length,
          totalCitizens: citizens.length,
          finalizedJudgments: (judgments as any[]).filter((j: any) => j.status === 'FINAL').length
        }));
        this.recentCases.set(cases as any[]);
        this.recentHearings.set((hearings as any[]).slice(-5).reverse());
        this.loading.set(false);
      });
    }
  }

  badge(s: string): string {
    const m: Record<string, string> = {
      FILED: 'bg-secondary', ACTIVE: 'bg-success', CLOSED: 'bg-dark',
      UNDER_REVIEW: 'bg-warning text-dark', HEARING_SCHEDULED: 'bg-info text-dark',
      JUDGMENT_PENDING: 'bg-danger', DISMISSED: 'bg-secondary',
      SCHEDULED: 'bg-primary', COMPLETED: 'bg-success',
      ADJOURNED: 'bg-warning text-dark', CANCELLED: 'bg-danger',
      DRAFT: 'bg-warning text-dark', FINAL: 'bg-success'
    };
    return m[s] ?? 'bg-secondary';
  }
}