import { ScreeningState } from "./screening.enums";

export interface Screening {
id: number;
programId: number;

// Public-safe fields
title: string;
genre?: string | null;
room?: string | null;
scheduledTime?: string | null;

// Full-detail fields (role-aware; may be omitted in public DTOs)
submitterId?: number;
description?: string | null;

state?: ScreeningState;

staffMemberId?: number | null;
submittedTime?: string | null;
reviewedTime?: string | null;

// (Optional future-proofing if backend sends these)
startTime?: string | null;
endTime?: string | null;
}
