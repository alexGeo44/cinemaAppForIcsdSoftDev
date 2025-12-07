// src/types.ts

// ===== ROLES =====
export type BaseRole =
| "VISITOR"
| "USER"
| "PROGRAMMER"
| "STAFF"
| "SUBMITTER"
| "ADMIN";

// ===== AUTH / USERS =====

export interface UserResponse {
id: number;
userName: string;        // αν στο backend είναι "username", άλλαξέ το ΚΑΙ εδώ
fullName: string;
role: BaseRole | string;
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

// ===== PROGRAMS =====

export type ProgramState =
| "CREATED"
| "SUBMISSION"
| "ASSIGNMENT"
| "REVIEW"
| "SCHEDULING"
| "FINAL_PUBLICATION"
| "DECISION"
| "ANNOUNCED";

export interface ProgramResponse {
id: number;
name: string;
description: string;
startDate: string;       // LocalDate -> string (ISO)
endDate: string;
state: ProgramState | string;
}

export interface CreateProgramRequest {
name: string;
description: string;
startDate: string;
endDate: string;
}

export interface UpdateProgramRequest extends CreateProgramRequest {}

export interface ProgramSearchFilters {
name?: string;
description?: string;
// πρόσθεσε κι άλλα αν τα χρησιμοποιείς σε programsApi.search
}

// ===== SCREENINGS =====

export type ScreeningState =
| "CREATED"
| "SUBMITTED"
| "REVIEWED"
| "APPROVED"
| "SCHEDULED"
| "REJECTED";

export interface ScreeningResponse {
id: number;
programId: number;
submitterId: number;
title: string;
genre: string;
description: string;
room?: string | null;
scheduledTime?: string | null;
state: ScreeningState | string;
staffMemberId?: number | null;
submittedTime?: string | null;
reviewedTime?: string | null;
}

export interface CreateScreeningRequest {
title: string;
genre: string;
description: string;
}

export interface UpdateScreeningRequest extends CreateScreeningRequest {}
