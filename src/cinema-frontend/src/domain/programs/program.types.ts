import { ProgramState } from "./program.enums";

export interface Program {
id: number;
name: string;
description: string | null;
startDate: string | null; // ISO yyyy-MM-dd
endDate: string | null;
state: ProgramState;
}

// re-export για να μπορείς να κάνεις:
// import { Program, ProgramState } from "../../domain/programs/program.types";
export { ProgramState };
