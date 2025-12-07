import { Chip } from "@mui/material";

const stateColors: Record<string, "default" | "success" | "warning" | "error"> = {
  CREATED: "default",
  SUBMISSION: "warning",
  ASSIGNMENT: "warning",
  REVIEW: "warning",
  SCHEDULING: "warning",
  FINAL_PUBLICATION: "warning",
  DECISION: "warning",
  ANNOUNCED: "success",
};

const StateChip = ({ state }: { state: string }) => (
  <Chip label={state} color={stateColors[state] ?? "default"} size="small" />
);

export default StateChip;
