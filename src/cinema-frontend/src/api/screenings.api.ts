import axios from "./axios";
import { Screening } from "../domain/screenings/screening.types";
import { ScreeningState } from "../domain/screenings/screening.enums";

export const screeningsApi = {
// POST /api/screenings?userId=&programId=
create: (
    userId: number,
    programId: number,
    data: { title: string; genre: string; description: string }
  ) =>
    axios.post<void>("/api/screenings", data, {
      params: { userId, programId },
    }),

  // PUT /api/screenings/{id}?callerId=
  update: (
    callerId: number,
    screeningId: number,
    data: { title: string; genre: string; description: string }
  ) =>
    axios.put<void>(`/api/screenings/${screeningId}`, data, {
      params: { callerId },
    }),

  // PUT /api/screenings/{id}/submit?callerId=
  submit: (callerId: number, screeningId: number) =>
    axios.put<void>(`/api/screenings/${screeningId}/submit`, null, {
      params: { callerId },
    }),

  // PUT /api/screenings/{id}/withdraw?userId=
  withdraw: (userId: number, screeningId: number) =>
    axios.put<void>(`/api/screenings/${screeningId}/withdraw`, null, {
      params: { userId },
    }),

  // PUT /api/screenings/{id}/assign/{staffId}?callerId=
  assignHandler: (
    callerId: number,
    screeningId: number,
    staffId: number
  ) =>
    axios.put<void>(`/api/screenings/${screeningId}/assign/${staffId}`, null, {
      params: { callerId /*, staffId */ },
    }),

  // PUT /api/screenings/{id}/accept?programmerId=&date=&room=
  acceptAndSchedule: (
    programmerId: number,
    screeningId: number,
    date: string, // ISO yyyy-MM-dd
    room: string
  ) =>
    axios.put<void>(`/api/screenings/${screeningId}/accept`, null, {
      params: { programmerId, date, room },
    }),

  // PUT /api/screenings/{id}/reject?staffId=
  reject: (staffId: number, screeningId: number) =>
    axios.put<void>(`/api/screenings/${screeningId}/reject`, null, {
      params: { staffId },
    }),

  // GET /api/screenings/{id}
  view: (id: number) =>
    axios.get<Screening>(`/api/screenings/${id}`),


byProgram: (params: {
    programId: number;
    state?: ScreeningState;
    offset?: number;
    limit?: number;
  }) =>
    axios.get<Screening[]>("/api/screenings/by-program", { params }),

  bySubmitter: (params: {
    submitterId: number;
    state?: ScreeningState;
    offset?: number;
    limit?: number;
  }) =>
    axios.get<Screening[]>("/api/screenings/by-submitter", { params }),

  byStaff: (params: {
    staffId: number;
    offset?: number;
    limit?: number;
  }) =>
    axios.get<Screening[]>("/api/screenings/by-staff", { params }),










};
