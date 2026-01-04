// src/api/programs.api.ts
import axios from "./axios";
import type { ProgramState } from "../domain/programs/program.enums";

export type ProgramViewResponse = {
id: number | null;
name: string;
description: string | null;
startDate: string | null; // yyyy-MM-dd
endDate: string | null;   // yyyy-MM-dd
state: string;            // ProgramState string

// Role-aware: may be absent for visitors/non-programmers
programmerIds?: number[];
staffIds?: number[];

creatorUserId?: number;
};

export type ProgramResponse = ProgramViewResponse;

export type CreateProgramRequest = {
name: string;
description: string;
startDate: string; // yyyy-MM-dd
endDate: string;   // yyyy-MM-dd
};

// Update can allow partial updates (backend likely accepts null/omitted)
export type UpdateProgramRequest = {
name?: string;
description?: string;
startDate?: string | null; // allow clearing if supported
endDate?: string | null;
};

export type ChangeProgramStateRequest = { nextState: string };

export const programsApi = {
// CREATE
create: (data: CreateProgramRequest) => axios.post<void>("/api/programs", data),

  // UPDATE
  update: (id: number, data: UpdateProgramRequest) =>
    axios.put<void>(`/api/programs/${id}`, data),

  // DELETE
  delete: (id: number) => axios.delete<void>(`/api/programs/${id}`),

  // SEARCH (AND semantics on supplied params)
  search: (params?: {
    name?: string;
    description?: string;
    from?: string;       // yyyy-MM-dd
    to?: string;         // yyyy-MM-dd
    filmTitle?: string;
    auditorium?: string;
    offset?: number;
    limit?: number;
  }) => axios.get<ProgramViewResponse[]>("/api/programs", { params }),

  // VIEW (role-aware DTO)
  view: (id: number) => axios.get<ProgramViewResponse>(`/api/programs/${id}`),

  // STATE transition
  changeState: (programId: number, nextState: ProgramState) =>
    axios.put<ProgramResponse>(`/api/programs/${programId}/state`, {
      nextState,
    } satisfies ChangeProgramStateRequest),

  // ROLE assignments
  addProgrammer: (programId: number, userId: number) =>
    axios.post<void>(`/api/programs/${programId}/programmers/${userId}`, null),

  addStaff: (programId: number, userId: number) =>
    axios.post<void>(`/api/programs/${programId}/staff/${userId}`, null),
};
