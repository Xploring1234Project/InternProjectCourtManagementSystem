import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { CaseResponse, CourtOrderResponse, JudgmentResponse, UserResponse } from '../../../shared/models';
import { JudgmentApiService } from '../../../core/services/judgment/judgment-api';
import { CaseApiService } from '../../../core/services/case/case-api';
import { UserApiService } from '../../../core/services/user/user-api';
import { CourtOrderApiService } from '../../../core/services/courtorder/courtorder-api';
import { AuthService } from '../../../core/services/auth/auth';

// --- View Models for UI Binding ---
export interface JudgmentViewModel extends JudgmentResponse {
  caseTitle: string;
  judgeName: string;
}

export interface CourtOrderViewModel extends CourtOrderResponse {
  caseTitle: string;
  judgeName: string;
}

@Component({
  selector: 'app-judgments-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './judgments-list.html',
  styleUrls: ['./judgments-list.css']
})
export class JudgmentsList implements OnInit {
  tab = signal<'judgments' | 'orders'>('judgments');
  
  // Use ViewModels instead of base responses for the UI
  judgments = signal<JudgmentViewModel[]>([]);
  orders = signal<CourtOrderViewModel[]>([]);
  cases = signal<CaseResponse[]>([]);
  judges = signal<UserResponse[]>([]);
  
  loading = signal<boolean>(true);
  ordersLoading = signal<boolean>(false);
  
  selectedJudgment = signal<JudgmentViewModel | null>(null);
  selectedOrder = signal<CourtOrderViewModel | null>(null);

  constructor(
    private judgmentApi: JudgmentApiService, 
    private orderApi: CourtOrderApiService,
    private caseApi: CaseApiService, 
    private userApi: UserApiService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    forkJoin({
      casesList: this.caseApi.getAll(),
      judgesList: this.userApi.getByRole('JUDGE'),
      judgmentsList: this.judgmentApi.getAll()
    }).subscribe({
      next: ({ casesList, judgesList, judgmentsList }) => {
        this.cases.set(casesList);
        this.judges.set(judgesList);
        
        const currentJudgeId = this.authService.currentUser?.userId;
        // Filter judgments for judges, show all for admins
        const filteredJudgments = this.isJudge && currentJudgeId != null
          ? judgmentsList.filter(judgmentItem => judgmentItem.judgeId === currentJudgeId)
          : judgmentsList;

        this.judgments.set(
          filteredJudgments.map(judgmentItem => ({
            ...judgmentItem, // spread operator
            caseTitle: casesList.find(caseItem => caseItem.caseId === judgmentItem.caseId)?.title || 'Unknown Case',
            judgeName: judgesList.find(judgeUser => judgeUser.userId === judgmentItem.judgeId)?.name || 'Unknown Judge'
          } as JudgmentViewModel))
        );
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  get isJudge(): boolean {
    return this.authService.hasRole('JUDGE');
  }

  loadOrders(): void {
    if (this.orders().length) return;
    // getting current judge id to filter orders if user is a judge
    const currentJudgeId = this.authService.currentUser?.userId;
    this.ordersLoading.set(true);
    this.orderApi.getAll().subscribe({ 
      next: ordersList => {
        // Filter orders for judges, show all for admins
        const filteredOrders = this.isJudge && currentJudgeId != null
          ? ordersList.filter(orderItem => orderItem.judgeId === currentJudgeId)
          : ordersList;

        this.orders.set(
          filteredOrders.map(orderItem => ({
            ...orderItem,
            caseTitle: this.cases().find(caseItem => caseItem.caseId === orderItem.caseId)?.title || 'Unknown Case',
            judgeName: this.judges().find(judgeUser => judgeUser.userId === orderItem.judgeId)?.name || 'Unknown Judge'
          } as CourtOrderViewModel))
        );
        this.ordersLoading.set(false); 
      }, 
      error: () => this.ordersLoading.set(false) 
    });
  }

  finalize(id: number): void {
    this.judgmentApi.finalize(id).subscribe(updatedJudgment => {
      this.judgments.update(currentJudgments => 
        currentJudgments.map(judgmentItem => judgmentItem.judgmentId === id 
          ? { 
              ...updatedJudgment, 
              caseTitle: judgmentItem.caseTitle, // preserve mapped field
              judgeName: judgmentItem.judgeName  // preserve mapped field
            } as JudgmentViewModel 
          : judgmentItem
        )
      );
    });
  }

  updateOrder(id: number, status: string): void {
    this.orderApi.updateStatus(id, status).subscribe(updatedOrder => {
      this.orders.update(currentOrders => 
        currentOrders.map(orderItem => orderItem.orderId === id 
          ? { 
              ...updatedOrder, 
              caseTitle: orderItem.caseTitle, // preserve mapped field
              judgeName: orderItem.judgeName  // preserve mapped field
            } as CourtOrderViewModel
          : orderItem
        )
      );
    });
  }

  viewJudgment(judgmentItem: JudgmentViewModel): void {
    this.selectedJudgment.set(judgmentItem);
  }

  viewOrder(orderItem: CourtOrderViewModel): void {
    this.selectedOrder.set(orderItem);
  }

  closeModal(): void {
    this.selectedJudgment.set(null);
    this.selectedOrder.set(null);
  }
}
