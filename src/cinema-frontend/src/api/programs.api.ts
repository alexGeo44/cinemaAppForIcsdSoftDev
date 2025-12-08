import axios from "./axios";
import { Program, ProgramState } from "../domain/programs/program.types";

export const programsApi = {
create: (creatorId: number, data: {
    name: string;
    description: string;
    startDate?: string;
    endDate?: string;
  }) =>
    axios.post("/api/programs", data, {
      params: { creatorId },
    }),

  update: (id: number, actorUserId: number, data: {
    name: string;
    description: string;
    startDate?: string;
    endDate?: string;
  }) =>
    axios.put(`/api/programs/${id}`, data, {
      params: { actorUserId },
    }),

  delete: (id: number, userId: number) =>
    axios.delete(`/api/programs/${id}`, {
      params: { userId },
    }),

  view: (id: number) =>
    axios.get<Program>(`/api/programs/${id}`),

  search: (params?: {
    name?: string;
    programState?: ProgramState;
    from?: string;
    to?: string;
    offset?: number;
    limit?: number;
  }) =>
    axios.get<Program[]>("/api/programs", { params }),

  addProgrammer: (
    id: number,
    ownerId: number,
    userId: number
  ) =>
    axios.post(`/api/programs/${id}/programmers/${userId}`, null, {
      params: { ownerId },
    }),

  addStaff: (
    id: number,
    programmerId: number,
    userId: number
  ) =>
    axios.post(`/api/programs/${id}/staff/${userId}`, null, {
      params: { programmerId },
    }),

  changeState: (
    id: number,
    newState: ProgramState,
    actorUserId: number
  ) =>
    axios.put(`/api/programs/${id}/state`, null, {
      params: { newState, actorUserId },
    }),
};
