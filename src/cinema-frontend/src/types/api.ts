// Base roles από BaseRole enum
export type BaseRole =
| "VISITOR"
| "USER"
| "PROGRAMMER"
| "STAFF"
| "SUBMITTER"
| "ADMIN";

// ===== USERS / AUTH =====

export interface UserResponse {
id: number;
userName: string;
fullName: string;
role: BaseRole;
}

export interface AuthResponse {
token: string;
user: UserResponse;
}

export interface LoginRequest {
username: string;
password: string;
}

export interface RegisterUserRequest {
username: string;
password: string;
fullName: string;
}

export interface TokenInfoResponse {
userId: number;
role: BaseRole;
}

// ===== PROGRAMS =====

export type ProgramState =
| "ACTIVE"
| "CANCELLED"
| "DRAFT"
| "SUBMISSION"
| "REVIEW"
| "SCHEDULING"
| "FINALIZED"
| "ARCHIVED";

export interface ProgramResponse {
id: number;
name: string;
description: string;
startDate: string; // LocalDate -> string (ISO)
endDate: string;
state: ProgramState | string; // security, just in case
}

export interface CreateProgramRequest {
name: string;
description: string;
startDate: string;
endDate: string;
}

export interface UpdateProgramRequest extends CreateProgramRequest {}

// ===== SCREENINGS =====

export type ScreeningState =
| "CREATED"
| "SUBMITTED"
| "UNDER_REVIEW"
| "ACCEPTED"
| "REJECTED"
| "SCHEDULED"
| "COMPLETED"
| "CANCELLED";

export interface ScreeningResponse {
id: number;
programId: number;
submitterId: number;
title: string;
genre: string;
description: string;
room: string | null;
scheduledTime: string | null;
state: ScreeningState | string;
staffMemberId: number | null;
submittedTime: string | null;
reviewedTime: string | null;
}

export interface CreateScreeningRequest {
title: string;
genre: string;
description: string;
}

export interface UpdateScreeningRequest extends CreateScreeningRequest {}
