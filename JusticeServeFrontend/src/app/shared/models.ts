// ── Auth ──────────────────────────────────────────────────────────
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  phone: string;
}


export interface AuthResponse {
  token: string;
  userId: number;
  name: string;
  email: string;
  role: string;
}

export interface UserResponse {
  userId: number;
  name: string;
  email: string;
  role: string;
  status: string;
  password: string;
}

/** Matches CitizenRequest.java */
export interface CitizenRequest {
  userId: number;
  name: string;
  dob: string;        // Format: 'YYYY-MM-DD'
  gender: string;
  address: string;
  contactInfo: string;
}

/** Matches CitizenResponse.java */
export interface CitizenResponse {
  citizenId: number;
  userId: number;
  name: string;
  dob: string;
  gender: string;
  address: string;
  contactInfo: string;
  status: string;
  createdAt: string;  // ISO Date string
}

/** Matches DocumentRequest.java */
export interface DocumentRequest {
  docType: string;    // Should match your DocType Enum (e.g., 'AADHAAR', 'PAN')
  fileUri: string;
}

/** Matches DocumentResponse.java */
export interface DocumentResponse {
  documentId: number;
  docType: string;
  fileUri: string;
  uploadedDate: string;
  verificationStatus: string; // e.g., 'PENDING', 'VERIFIED', 'REJECTED'
}

/** Matches CaseRequest.java */
export interface CaseRequest {
  citizenId: number;
  lawyerId?: number;  // Optional as per DTO
  title: string;
  description: string;
}

/** Matches CaseResponse.java */
export interface CaseResponse {
  caseId: number;
  citizenId: number;
  citizenName?: string;
  lawyerId?: number;
  lawyerName?: string;
  judgeId?: number;
  judgeName?: string;
  title: string;
  description: string;
  filedDate: string;  // LocalDate -> string
  status: string;     // Enum string (e.g., 'FILED', 'PENDING', 'CLOSED')
  createdAt: string;  // LocalDateTime -> string
}

/** Matches DocumentRequest.java (specific to Case Service) */
export interface CaseDocumentRequest {
  docType: string;    // Enum string
  fileUri: string;
}

/** Matches DocumentResponse.java (specific to Case Service) */
export interface CaseDocumentResponse {
  documentId: number;
  docType: string;
  fileUri: string;
  uploadedDate: string;
  verificationStatus: string;
}

/** Matches HearingRequest.java */
export interface HearingRequest {
  caseId: number;
  judgeId: number;
  date: string; // LocalDate -> 'YYYY-MM-DD'
  time: string; // LocalTime -> 'HH:mm:ss' or 'HH:mm'
}

/** Matches HearingResponse.java */
export interface HearingResponse {
  hearingId: number;
  caseId: number;
  judgeId: number;
  judgeName?: string;
  citizenId?: number;
  lawyerId?: number;
  caseTitle?: string;
  date: string;
  time: string;
  status: string; // Enum: SCHEDULED, ONGOING, COMPLETED, ADJOURNED, CANCELLED
}

/** Matches ProceedingRequest.java */
export interface ProceedingRequest {
  hearingId?: number; // Optional because the service can set it from the path
  notes: string;
  status: string;
}

/** Matches ProceedingResponse.java */
export interface ProceedingResponse {
  proceedingId: number;
  hearingId: number;
  notes: string;
  date: string;   // LocalDate
  status: string;
}


/** Matches JudgmentRequest.java */
export interface JudgmentRequest {
  caseId: number;
  judgeId: number;
  summary: string;
}

/** Matches JudgmentResponse.java */
export interface JudgmentResponse {
  judgmentId: number;
  caseId: number;
  judgeId: number;
  summary: string;
  date: string;      // LocalDate
  status: string;    // Enum: e.g., 'DRAFT', 'FINALIZED'
  caseTitle?: string;
}

/** Matches CourtOrderRequest.java */
export interface CourtOrderRequest {
  caseId: number;
  judgeId: number;
  description: string;
}

/** Matches CourtOrderResponse.java */
export interface CourtOrderResponse {
  orderId: number;
  caseId: number;
  judgeId: number;
  description: string;
  date: string;      // LocalDate
  status: string;    // Enum: OrderStatus
}

/** Matches NotificationRequest.java */
export interface NotificationRequest {
  userId: number;
  entityId?: number; // Optional (e.g., might not have a case ID for a general alert)
  message: string;
  category: 'CASE' | 'HEARING' | 'JUDGMENT' | 'COMPLIANCE';
}

/** Matches NotificationResponse.java */
export interface NotificationResponse {
  notificationId: number;
  userId: number;
  entityId?: number;
  message: string;
  category: 'CASE' | 'HEARING' | 'JUDGMENT' | 'COMPLIANCE';
  status: 'UNREAD' | 'READ';
  createdDate: string; // From LocalDateTime
}


/** 
 * Matches ReportScope Enum in Report.java 
 */
export type ReportScope = 'CASE' | 'HEARING' | 'JUDGMENT' | 'COMPLIANCE';

/** 
 * Matches Report.java Entity 
 */
export interface ReportResponse {
  reportId?: number;        // Optional for creation, present in responses
  scope: ReportScope;
  metrics: string;          // Stored as TEXT (JSON string or raw text)
  generatedDate: string;    // From LocalDate ('YYYY-MM-DD')
  generatedBy: number;      // User ID of the person who generated it
}

// --- Compliance Records ---

export interface ComplianceRecordRequest {
  entityId: number;
  type: 'CASE' | 'HEARING' | 'JUDGMENT' | 'USER'; // Matches ComplianceType Enum
  result: 'PASSED' | 'FAILED' | 'PENDING';        // Matches ComplianceResult Enum
  notes?: string;
}

export interface ComplianceRecordResponse {
  complianceId: number;
  entityId: number;
  type: string;
  result: string;
  date: string; // LocalDate -> 'YYYY-MM-DD'
  notes: string;
}

// --- Audits ---

export interface AuditRequest {
  officerId: number;
  scope: string;
  findings?: string;
}

export interface AuditResponse {
  auditId: number;
  officerId: number;
  scope: string;
  findings: string;
  date: string;   // LocalDate -> 'YYYY-MM-DD'
  status: string; // Enum: e.g., 'IN_PROGRESS', 'COMPLETED', 'FLAGGED'
}


/** Matches AuditLogResponse.java */
export interface AuditLogResponse {
  auditLogId: number;
  userId: number;
  action: string;
  resource: string;
  timestamp: string; // From LocalDateTime (e.g., "2026-05-05T12:00:00")
}