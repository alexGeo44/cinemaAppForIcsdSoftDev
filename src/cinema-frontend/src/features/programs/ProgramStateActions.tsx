import { Program, ProgramState } from "../../domain/programs/program.types";
import { programsApi } from "../../api/programs.api";
import { authStore } from "../../auth/auth.store";

const transitions: Record<ProgramState, ProgramState[]> = {
  DRAFT: [ProgramState.ACTIVE, ProgramState.CANCELLED],
  ACTIVE: [ProgramState.ARCHIVED, ProgramState.CANCELLED],
  ARCHIVED: [],
  CANCELLED: [],
  SUBMISSION: [],
  REVIEW: [],
  SCHEDULING: [],
  FINALIZED: [],
};

export function ProgramStateActions({ program }: { program: Program }) {
  const user = authStore(s => s.user);

  if (!user) return null;

  const possible = transitions[program.state] || [];

  return (
    <div>
      <h3>Actions</h3>
      {possible.map(state => (
        <button
          key={state}
          onClick={() =>
            programsApi.changeState(program.id, state, user.id)
          }
        >
          Change to {state}
        </button>
      ))}
    </div>
  );
}
