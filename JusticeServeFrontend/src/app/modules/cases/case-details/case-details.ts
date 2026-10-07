import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CaseResponse, DocumentResponse, HearingResponse, JudgmentResponse, ProceedingResponse, UserResponse } from '../../../shared/models';
import { CaseApiService } from '../../../core/services/case/case-api';
import { HearingApiService } from '../../../core/services/hearing/hearing-api';
import { JudgmentApiService } from '../../../core/services/judgment/judgment-api';
import { UserApiService } from '../../../core/services/user/user-api';
import { CitizenApiService } from '../../../core/services/citizen/citizen-api';
import { NotificationApiService } from '../../../core/services/notification/notification-api';
import { AuthService } from '../../../core/services/auth/auth';


@Component({
  selector: 'app-case-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, DatePipe],
  templateUrl: './case-details.html',
  styleUrls: ['./case-details.css']
})
export class CaseDetails implements OnInit {
  caseId = signal<number>(0);
  caseData = signal<CaseResponse>(null as any);
  documents = signal<DocumentResponse[]>([]);
  hearings = signal<HearingResponse[]>([]);
  judgments = signal<JudgmentResponse[]>([]);
  proceedings = signal<ProceedingResponse[]>([]);
  selectedProceeding = signal<ProceedingResponse | undefined>(undefined);
  showProceedingModal = signal<boolean>(false);
  lawyers = signal<UserResponse[]>([]);
  judges = signal<UserResponse[]>([]);

  loading = signal<boolean>(true);
  savingLawyer = signal<boolean>(false);
  savingJudge = signal<boolean>(false);
  updatingStatus = signal<boolean>(false);
  showLawyerPanel = signal<boolean>(false); // for assigning/removing lawyer
  showJudgePanel = signal<boolean>(false); // for assigning judge show panal for assigning judge from judge list
  showDocForm = signal<boolean>(false); //for uploading document

  toast = signal<{ msg: string; type: 'success' | 'error' } | null>(null);

  // Reactive Forms
  lawyerForm: FormGroup;
  judgeForm: FormGroup;
  statusForm: FormGroup;
  addDocForm: FormGroup;
  requestDocFormGroup: FormGroup;
  clerkDocRequestFormGroup: FormGroup;

  // Form states
  showRequestDocForm = signal<boolean>(false);
  sendingDocRequest = signal<boolean>(false);
  showClerkDocRequestForm = signal<boolean>(false);
  sendingClerkDocRequest = signal<boolean>(false);

  statuses = ['FILED', 'UNDER_REVIEW', 'ACTIVE', 'HEARING_SCHEDULED', 'JUDGMENT_PENDING', 'CLOSED', 'DISMISSED'];
  docTypes = ['PETITION', 'EVIDENCE', 'ORDER', 'ID_PROOF', 'LEGAL_DOC'];

  computedTimeline = computed(() => {
    const statusOrder = ['FILED', 'UNDER_REVIEW', 'ACTIVE', 'HEARING_SCHEDULED', 'JUDGMENT_PENDING', 'CLOSED'];
    const caseDataVal = this.caseData();
    const idx = statusOrder.indexOf(caseDataVal?.status ?? '');
    const hasScheduledHearing = this.hearings().some(hearing => ['SCHEDULED', 'IN_PROGRESS', 'COMPLETED'].includes(hearing.status));
    const hasJudgment = this.judgments().length > 0;
    return [
      { label: 'Case Filed', done: idx >= 0 },
      { label: 'Lawyer Assigned', done: !!caseDataVal?.lawyerName },
      { label: 'Under Review / Active', done: idx >= 1 },
      { label: 'Hearing Scheduled', done: idx >= 3 || hasScheduledHearing },
      { label: 'Judgment Pending', done: idx >= 4 || hasJudgment },
      { label: 'Case Closed', done: idx >= 5 || caseDataVal?.status === 'CLOSED' || this.judgments().some(judgment => judgment.status === 'FINAL') },
    ];
  });

  reviewPeriod = computed(() => {
    const caseDataVal = this.caseData();
    if (!caseDataVal) return '';
    const filed = new Date(caseDataVal.filedDate);
    const judgmentList = this.judgments();
    const latestJudgment = judgmentList.length
      ? judgmentList.reduce((latest, judgment) => {
          const date = new Date(judgment.date);
          return latest > date ? latest : date;
        }, new Date(judgmentList[0].date))
      : null;
    const end = latestJudgment ?? new Date();
    const days = Math.max(0, Math.ceil((end.getTime() - filed.getTime()) / (1000 * 60 * 60 * 24)));
    return `${days} day${days === 1 ? '' : 's'} since filed`;
  });

  constructor(
    private route: ActivatedRoute,
    private caseApi: CaseApiService,
    private hearingApi: HearingApiService,
    private judgmentApi: JudgmentApiService,
    private userApi: UserApiService,
    private citizenApi: CitizenApiService,
    private notifApi: NotificationApiService,
    public authService: AuthService,
    private fb: FormBuilder
  ) {
    this.lawyerForm = this.fb.group({
      lawyerId: ['', Validators.required]
    });

    this.judgeForm = this.fb.group({
      judgeId: ['', Validators.required]
    });

    this.statusForm = this.fb.group({
      status: ['', Validators.required]
    });

    this.addDocForm = this.fb.group({
      docType: ['', Validators.required],
      fileUri: ['', Validators.required]
    });

    this.requestDocFormGroup = this.fb.group({
      requestedDocType: ['', Validators.required],
      subject: ['', Validators.required],
      content: ['', Validators.required]
    });

    this.clerkDocRequestFormGroup = this.fb.group({
      docType: ['', Validators.required],
      description: ['', Validators.required],
      requiredBy: ['', Validators.required]
    });
  }

  get isAdmin() { return this.authService.hasRole('ADMIN'); }
  get isClerk() { return this.authService.hasRole('CLERK'); }
  get isJudge() { return this.authService.hasRole('JUDGE'); }
  get isCitizen() { return this.authService.hasRole('CITIZEN'); }
  get isLawyer() { return this.authService.hasRole('LAWYER'); }
  get isCompliance() { return this.authService.hasRole('COMPLIANCE'); }
  get isAuditor() { return this.authService.hasRole('AUDITOR'); }
  get isReviewer() { return this.isCompliance || this.isAuditor; }
  get canAssignLawyer() { return this.isAdmin || this.isClerk; }
  get canAssignJudge() { return this.isAdmin || this.isClerk; }
  get canUpdateStatus() { return this.isAdmin || this.isClerk || this.isJudge; }
  get canVerifyDocs() { return this.isAdmin || this.isClerk; }
  get canScheduleHearing() { return this.isAdmin || this.isClerk || this.isJudge; }
  get isCaseClosed() { return this.caseData()?.status === 'CLOSED'; }

  ngOnInit(): void {
    this.caseId.set(+this.route.snapshot.params['id']);
    this.loadCase();
    this.caseApi.getDocuments(this.caseId()).subscribe(docs => this.documents.set(docs), () => {});
    
    this.hearingApi.getHearingsByCase(this.caseId()).subscribe({
      next: hearings => {
        this.hearings.set(hearings);
        if (hearings.length) {
          forkJoin(hearings.map(hearingItem => this.hearingApi.getProceedings(hearingItem.hearingId))).subscribe({
            next: proceedingGroups => this.proceedings.set(proceedingGroups.flat()),
            error: () => this.proceedings.set([])
          });
        } else {
          this.proceedings.set([]);
        }
      },
      error: () => { this.hearings.set([]); this.proceedings.set([]); }
    });

    this.judgmentApi.getByCaseId(this.caseId()).subscribe(judg => this.judgments.set(judg), () => {});
    
    if (this.canAssignLawyer || this.canAssignJudge) {
      this.userApi.getByRole('LAWYER').subscribe(data => this.lawyers.set(data), () => {});
      this.userApi.getByRole('JUDGE').subscribe(data => this.judges.set(data), () => {});
    }
  }

  loadCase(): void {
    this.loading.set(true);
    this.caseApi.getById(this.caseId()).subscribe({
      next: caseDetails => {
        this.caseData.set(caseDetails);
        this.statusForm.patchValue({ status: caseDetails.status });
        this.lawyerForm.patchValue({ lawyerId: caseDetails.lawyerId ? String(caseDetails.lawyerId) : '' });
        this.judgeForm.patchValue({ judgeId: caseDetails.judgeId ? String(caseDetails.judgeId) : '' });
        this.loading.set(false);
      },
      error: () => { this.loading.set(false); }
    });
  }

  toggleLawyerPanel(): void {
    this.showLawyerPanel.set(!this.showLawyerPanel());
    this.showJudgePanel.set(false);
  }

  toggleJudgePanel(): void {
    this.showJudgePanel.set(!this.showJudgePanel());
    //this.showJudgePanel.update(panel => !panel);
    this.showLawyerPanel.set(false);
  }

  assignLawyer(): void {
    if (this.lawyerForm.invalid) return;
    const lawyerIdNum = +this.lawyerForm.value.lawyerId;
    const lawyer = this.lawyers().find(l => +l.userId === lawyerIdNum);
    if (!lawyer) return;

    this.savingLawyer.set(true);
    this.caseApi.assignLawyer(this.caseId(), lawyerIdNum).subscribe({
      next: (updatedCase) => {
        this.caseData.set(updatedCase);
        this.statusForm.patchValue({ status: updatedCase.status });
        this.showLawyerPanel.set(false);
        this.savingLawyer.set(false);
        this.showToast(`Lawyer "${lawyer.name}" assigned successfully!`, 'success');

        this.notifApi.sendNotification({
          userId: lawyer.userId, entityId: this.caseId(), category: 'CASE',
          message: `You are assigned as lawyer for Case #${this.caseId()}: "${updatedCase.title}". Citizen: ${updatedCase.citizenName}.`
        }).subscribe();

        this.citizenApi.getById(updatedCase.citizenId).subscribe({
          next: citizen => {
            this.notifApi.sendNotification({
              userId: citizen.userId, entityId: this.caseId(), category: 'CASE',
              message: `Your case "${updatedCase.title}" now has lawyer ${lawyer.name} assigned.`
            }).subscribe();
          }, error: () => {}
        });
      },
      error: (err) => {
        this.savingLawyer.set(false);
        this.showToast(err.error?.message || 'Failed to assign lawyer.', 'error');
      }
    });
  }

  removeLawyer(): void {
    if (!confirm('Remove the assigned lawyer from this case?')) return;
    this.savingLawyer.set(true);
    this.caseApi.removeLawyer(this.caseId()).subscribe({
      next: (updatedCase) => {
        this.caseData.set(updatedCase);
        this.lawyerForm.reset();
        this.savingLawyer.set(false);
        this.showToast('Lawyer removed successfully.', 'success');
      },
      error: (err) => { this.savingLawyer.set(false); this.showToast(err.error?.message || 'Failed.', 'error'); }
    });
  }

  assignJudge(): void {
    if (this.judgeForm.invalid) return;
    const judgeIdNum = +this.judgeForm.value.judgeId;
    const judge = this.judges().find(j => +j.userId === judgeIdNum);
    if (!judge) return;

    this.savingJudge.set(true);
    this.caseApi.assignJudge(this.caseId(), judgeIdNum).subscribe({
      next: (updatedCase) => {
        this.caseData.set(updatedCase);
        this.showJudgePanel.set(false);
        this.savingJudge.set(false);
        this.showToast(`Judge "${judge.name}" assigned successfully!`, 'success');

        this.notifApi.sendNotification({
          userId: judge.userId,
          entityId: this.caseId(),
          category: 'CASE',
          message: `You are assigned as Judge for Case #${this.caseId()}: "${updatedCase.title}".`
        }).subscribe();

        if (updatedCase.lawyerId) {
          this.notifApi.sendNotification({
            userId: updatedCase.lawyerId,
            entityId: this.caseId(),
            category: 'CASE',
            message: `Judge ${judge.name} has been assigned to your Case #${this.caseId()}: "${updatedCase.title}".`
          }).subscribe();
        }

        this.citizenApi.getById(updatedCase.citizenId).subscribe({
          next: citizen => {
            this.notifApi.sendNotification({
              userId: citizen.userId,
              entityId: this.caseId(),
              category: 'CASE',
              message: `A Judge (${judge.name}) has been assigned to your case: "${updatedCase.title}".`
            }).subscribe();
          },
          error: () => console.warn('Could not notify citizen of judge assignment')
        });
      },
      error: (err) => {
        this.savingJudge.set(false);
        this.showToast(err.error?.message || 'Failed to assign judge.', 'error');
      }
    });
  }

  updateStatus(): void {
    if (this.statusForm.invalid) return;
    const newStatus = this.statusForm.value.status;
    this.updatingStatus.set(true);
    
    this.caseApi.updateStatus(this.caseId(), newStatus).subscribe({
      next: updatedCase => {
        this.caseData.set(updatedCase);
        this.statusForm.patchValue({ status: updatedCase.status });
        this.updatingStatus.set(false);
        this.showToast(`Status updated to ${updatedCase.status}`, 'success');
      },
      error: (err) => {
        this.updatingStatus.set(false);
        this.showToast(err.error?.message || 'Failed to update status.', 'error');
      }
    });
  }

  addDocument(): void {
    if (this.isCaseClosed) {
      this.showToast('Cannot add documents to a closed case.', 'error');
      return;
    }
    if (this.addDocForm.invalid) return;
    this.caseApi.uploadDocument(this.caseId(), this.addDocForm.value).subscribe({
      next: docResponse => {
        this.documents.update(docs => [...docs, docResponse]);
        this.showDocForm.set(false);
        this.addDocForm.reset();
        this.showToast('Document added successfully.', 'success');
      },
      error: (err) => this.showToast(err.error?.message || 'Failed to add document.', 'error')
    });
  }

  verifyDoc(docId: number, status: string): void {
    this.caseApi.verifyDocument(docId, status).subscribe({
      next: docResponse => { this.documents.update(docs => docs.map(doc => doc.documentId === docId ? docResponse : doc)); },
      error: (err) => this.showToast(err.error?.message || 'Failed.', 'error')
    });
  }

  finalizeJudgment(judgmentId: number): void {
    if (this.isCaseClosed) {
      this.showToast('Cannot finalize a new judgment for a closed case.', 'error');
      return;
    }
    if (!confirm('Finalize this judgment? The case will be marked as CLOSED.')) return;
    this.judgmentApi.finalize(judgmentId).subscribe({
      next: judgResponse => {
        this.judgments.update(judg => judg.map(judgment => judgment.judgmentId === judgResponse.judgmentId ? judgResponse : judgment));
        this.showToast('Judgment finalized. Case will be closed.', 'success');
        this.caseApi.getById(this.caseId()).subscribe(caseDetails => { 
          this.caseData.set(caseDetails); 
          this.statusForm.patchValue({ status: caseDetails.status }); 
        });
      },
      error: (err) => this.showToast(err.error?.message || 'Failed.', 'error')
    });
  }

  showToast(msg: string, type: 'success' | 'error'): void {
    this.toast.set({ msg, type });
    setTimeout(() => this.toast.set(null), 4000);
  }

  badge(s: string): string {
    const statusMap: Record<string, string> = {
      FILED: 'bg-secondary', ACTIVE: 'bg-success', CLOSED: 'bg-dark',
      UNDER_REVIEW: 'bg-warning text-dark', HEARING_SCHEDULED: 'bg-info text-dark',
      JUDGMENT_PENDING: 'bg-danger', DISMISSED: 'bg-secondary',
      SCHEDULED: 'bg-primary', IN_PROGRESS: 'bg-info text-dark',
      COMPLETED: 'bg-success', ADJOURNED: 'bg-warning text-dark',
      CANCELLED: 'bg-danger', DRAFT: 'bg-warning text-dark', FINAL: 'bg-success'
    };
    return statusMap[s] ?? 'bg-secondary';
  }

  verifyBadge(status: string): string {
    return status === 'VERIFIED' ? 'bg-success' : status === 'REJECTED' ? 'bg-danger' : 'bg-warning text-dark';
  }

  fileIcon(fileUri: string): string {
    const ext = (fileUri || '').split('.').pop()?.toLowerCase() || '';
    const iconMap: Record<string, string> = {
      pdf: 'bi-file-earmark-pdf text-danger',
      doc: 'bi-file-earmark-word text-primary', docx: 'bi-file-earmark-word text-primary',
      jpg: 'bi-file-earmark-image text-success', jpeg: 'bi-file-earmark-image text-success',
      png: 'bi-file-earmark-image text-success',
      zip: 'bi-file-earmark-zip text-warning', rar: 'bi-file-earmark-zip text-warning'
    };
    return iconMap[ext] || 'bi-file-earmark';
  }

  getFileName(fileUri: string): string {
    if (!fileUri) return 'document';
    return fileUri.split('/').pop() || fileUri;
  }

  openProceeding(proceeding: ProceedingResponse): void {
    this.selectedProceeding.set(proceeding);
    this.showProceedingModal.set(true);
  }

  closeProceeding(): void {
    this.showProceedingModal.set(false);
    this.selectedProceeding.set(undefined);
  }

  getHearingTitle(proceeding: ProceedingResponse): string {
    const hearing = this.hearings().find(h => h.hearingId === proceeding.hearingId);
    return hearing ? `Hearing #${hearing.hearingId}` : `Hearing #${proceeding.hearingId}`;
  }

  getProceedingJudgeName(proceeding: ProceedingResponse): string {
    const hearing = this.hearings().find(h => h.hearingId === proceeding.hearingId);
    return hearing?.judgeName || this.caseData()?.judgeName || 'N/A';
  }

  sendDocumentRequest(): void {
    if (this.isCaseClosed) {
      this.showToast('Cannot request documents for a closed case.', 'error');
      return;
    }
    if (this.requestDocFormGroup.invalid) {
      this.showToast('Please fill in all required fields', 'error');
      return;
    }

    this.sendingDocRequest.set(true);
    const userId = this.authService.currentUser?.userId;
    if (!userId) {
      this.showToast('User not logged in', 'error');
      this.sendingDocRequest.set(false);
      return;
    }

    const formData = this.requestDocFormGroup.value;
    const notificationMessage =
      `Document Request for Case #${this.caseId()} - ${formData.requestedDocType}\n` +
      `Case Title: ${this.caseData().title}\n` +
      `Subject: ${formData.subject}\n` +
      `Details: ${formData.content}`;

    const notifyCitizen = (citizenUserId: number) => {
      return this.notifApi.sendNotification({
        userId: citizenUserId,
        entityId: this.caseId(),
        category: 'CASE',
        message: notificationMessage
      });
    };

    const notifyLawyer = () => {
      return this.notifApi.sendNotification({
        userId,
        entityId: this.caseId(),
        category: 'CASE',
        message:
          `You requested documents for Case #${this.caseId()} - ${formData.requestedDocType}\n` +
          `Case Title: ${this.caseData().title}\n` +
          `Case Description: ${this.caseData().description || 'No description provided.'}\n` +
          `Subject: ${formData.subject}\n` +
          `Details: ${formData.content}`
      });
    };

    this.citizenApi.getById(this.caseData().citizenId).subscribe({
      next: citizen => {
        notifyCitizen(citizen.userId).subscribe({
          next: () => {
            notifyLawyer().subscribe({
              next: () => {
                this.showRequestDocForm.set(false);
                this.requestDocFormGroup.reset();
                this.sendingDocRequest.set(false);
                this.showToast('Document request notification sent to citizen and saved for you.', 'success');
              },
              error: (err: any) => {
                console.error('Error notifying lawyer:', err);
                this.sendingDocRequest.set(false);
                this.showToast('Citizen was notified, but failed to notify lawyer.', 'error');
              }
            });
          },
          error: (err: any) => {
            console.error('Error creating citizen notification:', err);
            this.sendingDocRequest.set(false);
            this.showToast('Failed to send document request to citizen: ' + (err.error?.message || err.message), 'error');
          }
        });
      },
      error: (err: any) => {
        console.error('Error loading citizen user id:', err);
        this.sendingDocRequest.set(false);
        this.showToast('Unable to resolve citizen for notification.', 'error');
      }
    });
  }

  sendClerkDocumentRequest(): void {
    if (this.isCaseClosed) {
      this.showToast('Cannot request documents for a closed case.', 'error');
      return;
    }
    if (this.clerkDocRequestFormGroup.invalid) {
      this.showToast('Please fill in all required fields', 'error');
      return;
    }
    if (!this.caseData().lawyerId) {
      this.showToast('No lawyer assigned to this case', 'error');
      return;
    }

    const clerkId = this.authService.currentUser?.userId;
    if (!clerkId) {
      this.showToast('User not logged in', 'error');
      return;
    }

    this.sendingClerkDocRequest.set(true);
    const formData = this.clerkDocRequestFormGroup.value;

    const notificationMessage =
      `Document Request from Clerk\nRequested Document Type: ${formData.docType}\nCase Title: ${this.caseData().title}\nDescription: ${formData.description}\nRequired By: ${formData.requiredBy}\nRequested By: ${this.authService.currentUser?.name || 'Court Clerk'}`;

    this.notifApi.sendNotification({
      userId: this.caseData().lawyerId!,
      entityId: this.caseId(),
      category: 'CASE',
      message: notificationMessage
    }).subscribe({
      next: () => {
        this.showToast('Document request sent to lawyer successfully', 'success');
        this.showClerkDocRequestForm.set(false);
        this.clerkDocRequestFormGroup.reset();
        this.sendingClerkDocRequest.set(false);
      },
      error: (err) => {
        console.error('Error sending document request notification:', err);
        this.showToast('Failed to send document request: ' + (err.error?.message || err.message), 'error');
        this.sendingClerkDocRequest.set(false);
      }
    });
  }
}