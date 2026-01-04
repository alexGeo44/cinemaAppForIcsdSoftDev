import { ProgramState } from "./program.enums";

export type ISODate = `${number}-${number}-${number}`;

export interface Program {
id: number;
name: string;
description: string | null;
startDate: ISODate | null; // yyyy-MM-dd
endDate: ISODate | null;   // yyyy-MM-dd
state: ProgramState;

// Role-aware fields (may be omitted for visitors/non-programmers depending on backend redaction)
programmerIds?: number[];
staffIds?: number[];
creatorUserId?: number;
}

// ✅ re-export
export { ProgramState };
