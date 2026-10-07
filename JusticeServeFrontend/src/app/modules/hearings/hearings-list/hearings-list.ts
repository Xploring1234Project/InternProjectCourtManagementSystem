import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { HearingResponse } from '../../../shared/models';
import { HearingApiService } from '../../../core/services/hearing/hearing-api';
import { CaseApiService } from '../../../core/services/case/case-api';
import { AuthService } from '../../../core/services/auth/auth';

@Component({
  selector: 'app-hearings-list',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './hearings-list.html',
  styleUrls: ['./hearings-list.css']
})
export class HearingsList implements OnInit {
  hearings = signal<HearingResponse[]>([]);
  loading = signal(true);
  statuses = ['SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'ADJOURNED', 'CANCELLED'];
  
  filterForm: FormGroup;

  constructor(
    private hearingApi: HearingApiService, 
    private caseApi: CaseApiService, 
    private authService: AuthService,
    private fb: FormBuilder
  ) {
    this.filterForm = this.fb.group({
      search: [''],
      filterStatus: ['']
    });
  }

  get isLawyer(): boolean { 
    return this.authService.hasRole('LAWYER'); 
  }

  get isJudge(): boolean {
    return this.authService.hasRole('JUDGE');
  }

  get filtered(): HearingResponse[] {
    const searchValue = this.filterForm.get('search')?.value?.toLowerCase() || '';
    const filterStatusValue = this.filterForm.get('filterStatus')?.value || '';

    return this.hearings().filter(hearing =>
      (!filterStatusValue || hearing.status === filterStatusValue) &&
      (!searchValue || hearing.caseTitle?.toLowerCase().includes(searchValue))
    );
  }

  ngOnInit(): void {
    if (this.isLawyer) {
      // For lawyers, fetch only hearings for cases assigned to the current lawyer
      this.caseApi.getAll().subscribe({
        next: cases => {
          const userId = this.authService.currentUser?.userId;
          if (!userId) {
            this.loading.set(false);
            return;
          }

          const myCases = cases.filter(caseItem => caseItem.lawyerId === userId);
          if (myCases.length === 0) {
            this.hearings.set([]);
            this.loading.set(false);
            return;
          }

          const allHearings: HearingResponse[] = [];
          let pending = myCases.length;

          myCases.forEach(caseItem => {
            this.hearingApi.getHearingsByCase(caseItem.caseId).subscribe({
              next: caseHearings => {
                allHearings.push(...caseHearings);
                pending--;
                if (pending === 0) {
                  this.hearings.set(allHearings);
                  this.loading.set(false);
                }
              },
              error: () => {
                pending--;
                if (pending === 0) {
                  this.hearings.set(allHearings);
                  this.loading.set(false);
                }
              }
            });
          });
        },
        error: () => this.loading.set(false)
      });
    } else if (this.isJudge) {
      // For judges, fetch only hearings assigned to the current judge
      const userId = this.authService.currentUser?.userId;
      if (!userId) {
        this.hearings.set([]);
        this.loading.set(false);
        return;
      }

      this.hearingApi.getHearingsByJudge(userId).subscribe({
        next: hearingsData => {
          this.hearings.set(hearingsData);
          this.loading.set(false);
        },
        error: () => this.loading.set(false)
      });
    } else {
      this.hearingApi.getAllHearings().subscribe({
        next: hearingsData => {
          this.hearings.set(hearingsData);
          this.loading.set(false);
        },
        error: () => this.loading.set(false)
      });
    }
  }

  badge(status: string): string {
    const statusMap: Record<string, string> = { 
      SCHEDULED: 'bg-primary', 
      IN_PROGRESS: 'bg-info text-dark', 
      COMPLETED: 'bg-success', 
      ADJOURNED: 'bg-warning text-dark', 
      CANCELLED: 'bg-danger' 
    };
    return statusMap[status] ?? 'bg-secondary';
  }
}