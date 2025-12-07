export type ProgramState =
| "CREATED"
| "SUBMISSION"
| "ASSIGNMENT"
| "REVIEW"
| "SCHEDULING"
| "FINAL_PUBLICATION"
| "DECISION"
| "ANNOUNCED";

export interface ProgramRoleAssignments {
programmers: number[]; // user ids
staff: number[];
}

export interface Program {
id: number;
name: string;
description: string;
startDate: string;
endDate: string;
creationDate: string;
state: ProgramState;
auditorium?: string;
roleAssignments: ProgramRoleAssignments;
}

export interface ProgramSearchFilters {
name?: string;
description?: string;
filmTitle?: string;
auditorium?: string;
startDate?: string;
endDate?: string;
}
