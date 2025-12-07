import axiosClient from "./axiosClient";
import type {
CreateProgramRequest,
ProgramResponse,
ProgramSearchFilters,
ProgramState,
UpdateProgramRequest,
} from "../types";

// Αν στο backend περνάς userId/actorUserId σαν query param,
// πρόσθεσέ το στο params (π.χ. { params: { actorUserId: user.id } })

export const programsApi = {
create: (data: CreateProgramRequest) =>
    axiosClient.post<void>("/programs", data).then((r) => r.data),

  update: (id: number, data: UpdateProgramRequest) =>
    axiosClient.put<void>(`/programs/${id}`, data).then((r) => r.data),

  delete: (id: number) =>
    axiosClient.delete<void>(`/programs/${id}`).then((r) => r.data),

  get: (id: number) =>
    axiosClient.get<ProgramResponse>(`/programs/${id}`).then((r) => r.data),

  search: (filters: ProgramSearchFilters) =>
    axiosClient
      .get<ProgramResponse[]>("/programs", { params: filters })
      .then((r) => r.data),

  changeState: (id: number, newState: ProgramState) =>
    axiosClient
      .put<void>(`/programs/${id}/state`, null, { params: { newState } })
      .then((r) => r.data),

  addProgrammer: (programId: number, userId: number) =>
    axiosClient
      .post<void>(`/programs/${programId}/programmers/${userId}`)
      .then((r) => r.data),

  addStaff: (programId: number, userId: number) =>
    axiosClient
      .post<void>(`/programs/${programId}/staff/${userId}`)
      .then((r) => r.data),
};
