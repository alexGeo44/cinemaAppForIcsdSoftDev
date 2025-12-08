import { ScreeningState } from "./screening.enums";

export interface Screening {
id: number;
programId: number;
submitterId: number;
title: string;
genre: string;
description: string;
room: string | null;
scheduledTime: string | null;
state: ScreeningState;
staffMemberId: number | null;
submittedTime: string | null;
reviewedTime: string | null;
}
