import axiosClient from "./axiosClient";
import type {
CreateScreeningRequest,
ScreeningResponse,
UpdateScreeningRequest,
} from "../types";

export const screeningsApi = {
create: (programId: number, data: CreateScreeningRequest) =>
    axiosClient
      .post<void>("/screenings", data, { params: { programId } })
      .then((r) => r.data),

  update: (id: number, data: UpdateScreeningRequest) =>
    axiosClient.put<void>(`/screenings/${id}`, data).then((r) => r.data),

  submit: (id: number) =>
    axiosClient.put<void>(`/screenings/${id}/submit`).then((r) => r.data),

  withdraw: (id: number) =>
    axiosClient.put<void>(`/screenings/${id}/withdraw`).then((r) => r.data),

  assignHandler: (id: number, staffId: number) =>
    axiosClient
      .put<void>(`/screenings/${id}/assign/${staffId}`)
      .then((r) => r.data),

  accept: (id: number) =>
    axiosClient.put<void>(`/screenings/${id}/accept`).then((r) => r.data),

  reject: (id: number) =>
    axiosClient.put<void>(`/screenings/${id}/reject`).then((r) => r.data),

  get: (id: number) =>
    axiosClient
      .get<ScreeningResponse>(`/screenings/${id}`)
      .then((r) => r.data),
};
