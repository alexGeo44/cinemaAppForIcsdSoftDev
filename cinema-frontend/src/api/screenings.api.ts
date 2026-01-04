// src/api/screenings.api.ts
import axios from "./axios";
import type { ScreeningState } from "../domain/screenings/screening.enums";
import type { Screening } from "../domain/screenings/screening.types";

export type CreateOrUpdateScreeningRequest = {
title: string;
genre?: string;
description?: string;

};

export const screeningsApi = {
// POST /api/screenings?programId=...
create: (programId: number, data: CreateOrUpdateScreeningRequest) =>
    axios.post<Screening>("/api/screenings", data, { params: { programId } }),

  // GET /api/screenings/{id}
  view: (id: number) => axios.get<Screening>(`/api/screenings/${id}`),

  // PUT /api/screenings/{id}
  update: (screeningId: number, data: CreateOrUpdateScreeningRequest) =>
    axios.put<void>(`/api/screenings/${screeningId}`, data),

  // PUT /api/screenings/{id}/submit
  submit: (screeningId: number) =>
    axios.put<void>(`/api/screenings/${screeningId}/submit`, null),

  // ✅ Withdraw = delete draft (CREATED) σύμφωνα με spec
  // DELETE /api/screenings/{id}
  withdraw: (screeningId: number) =>
    axios.delete<void>(`/api/screenings/${screeningId}`),

  // PUT /api/screenings/{id}/handler/{staffId}
  assignHandler: (screeningId: number, staffId: number) =>
    axios.put<void>(`/api/screenings/${screeningId}/handler/${staffId}`, null),

  // PUT /api/screenings/{id}/review?score=..&comments=..
  review: (screeningId: number, score: number, comments?: string) =>
    axios.put<void>(`/api/screenings/${screeningId}/review`, null, {
      params: { score, comments },
    }),

  // PUT /api/screenings/{id}/approve
  approve: (screeningId: number) =>
    axios.put<void>(`/api/screenings/${screeningId}/approve`, null),

  // PUT /api/screenings/{id}/final-submit
  finalSubmit: (screeningId: number) =>
    axios.put<void>(`/api/screenings/${screeningId}/final-submit`, null),

  // PUT /api/screenings/{id}/schedule?date=YYYY-MM-DD&room=Room%201
  schedule: (screeningId: number, date: string, room: string) =>
    axios.put<void>(`/api/screenings/${screeningId}/schedule`, null, {
      params: { date, room },
    }),

  // PUT /api/screenings/{id}/reject?reason=...
  reject: (screeningId: number, reason: string) =>
    axios.put<void>(`/api/screenings/${screeningId}/reject`, null, {
      params: { reason },
    }),

  // GET /api/screenings/by-program?programId=..&title=..&genre=..&from=..&to=..&state=..&offset=..&limit=..&timetable=true
  byProgram: (params: {
    programId: number;
    title?: string;
    genre?: string;
    from?: string; // yyyy-MM-dd
    to?: string; // yyyy-MM-dd
    state?: ScreeningState;
    offset?: number;
    limit?: number;
    timetable?: boolean;
  }) => axios.get<Screening[]>("/api/screenings/by-program", { params }),

  // GET /api/screenings/by-submitter?submitterId=..&state=..&offset=..&limit=..
  bySubmitter: (params?: {
    submitterId?: number;
    state?: ScreeningState;
    offset?: number;
    limit?: number;
  }) => axios.get<Screening[]>("/api/screenings/by-submitter", { params }),

  // GET /api/screenings/by-staff?offset=..&limit=..
  byStaff: (params?: { offset?: number; limit?: number }) =>
    axios.get<Screening[]>("/api/screenings/by-staff", { params }),
};
