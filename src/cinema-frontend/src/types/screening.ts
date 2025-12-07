export type ScreeningState =
| "CREATED"
| "SUBMITTED"
| "REVIEWED"
| "APPROVED"
| "SCHEDULED"
| "REJECTED";

export interface FilmInfo {
title: string;
cast: string;
genres: string;
durationMinutes: number;
}

export interface Screening {
id: number;
programId: number;
creationDate: string;
state: ScreeningState;
film: FilmInfo;
auditorium: string;
startTime?: string;
endTime?: string;
handlerStaffId?: number;
reviewScore?: number;
reviewComments?: string;
rejectionReason?: string;
submitterId: number;
}

export interface ScreeningSearchFilters {
programId: number;
title?: string;
cast?: string;
genre?: string;
fromDate?: string;
toDate?: string;
}
