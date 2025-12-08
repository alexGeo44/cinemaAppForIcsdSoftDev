export type PasswordViolationCode =
| "blank"
| "length"
| "upper"
| "lower"
| "digit"
| "special"
| "repeat"
| "sequence"
| "banned"
| "username-fragment"
| "name-fragment";

export interface PasswordViolation {
code: PasswordViolationCode;
message: string;
}

export interface PasswordValidationResult {
valid: boolean;
violations: PasswordViolation[];
}

const bannedPasswords = new Set(["password", "123456", "qwerty"]);

const config = {
minLength: 10,
requireUpper: true,
requireLower: true,
requireDigit: true,
requireSpecial: true,
maxRepeatSequence: 3,
maxAscendingSequence: 4,
};

export function validatePassword(
  rawPassword: string,
  username?: string | null,
  fullName?: string | null
): PasswordValidationResult {
  const violations: PasswordViolation[] = [];

  if (!rawPassword || rawPassword.trim() === "") {
    violations.push({
      code: "blank",
      message: "Password cannot be blank",
    });
    return { valid: false, violations };
  }

  const p = rawPassword;

  // length
  if (p.length < config.minLength) {
    violations.push({
      code: "length",
      message: `Minimum length: ${config.minLength}`,
    });
  }

  // upper
  if (config.requireUpper && !/[A-Z]/.test(p)) {
    violations.push({
      code: "upper",
      message: "At least one uppercase letter required",
    });
  }

  // lower
  if (config.requireLower && !/[a-z]/.test(p)) {
    violations.push({
      code: "lower",
      message: "At least one lowercase letter required",
    });
  }

  // digit
  if (config.requireDigit && !/\d/.test(p)) {
    violations.push({
      code: "digit",
      message: "At least one digit required",
    });
  }

  // special
  if (config.requireSpecial && !/[^A-Za-z0-9]/.test(p)) {
    violations.push({
      code: "special",
      message: "At least one special character required",
    });
  }

  // repeat sequence > maxRepeatSequence (άρα threshold = max+1)
  if (
    config.maxRepeatSequence > 0 &&
    hasRepeatSequence(p, config.maxRepeatSequence + 1)
  ) {
    violations.push({
      code: "repeat",
      message: "Too many repeated characters in a row",
    });
  }

  // ascending sequence length > maxAscendingSequence (threshold = max+1)
  if (
    config.maxAscendingSequence > 0 &&
    hasAscendingSequence(p, config.maxAscendingSequence + 1)
  ) {
    violations.push({
      code: "sequence",
      message: "Contains long ascending sequence",
    });
  }

  // banned/common
  if (bannedPasswords.has(p.toLowerCase())) {
    violations.push({
      code: "banned",
      message: "Password is too common",
    });
  }

  // contains username
  if (username && containsIgnoreCase(p, username)) {
    violations.push({
      code: "username-fragment",
      message: "Password must not contain the username",
    });
  }

  // contains full name token (>=3)
  if (fullName && anyTokenContained(p, fullName)) {
    violations.push({
      code: "name-fragment",
      message: "Password must not contain parts of your name",
    });
  }

  return {
    valid: violations.length === 0,
    violations,
  };
}

function hasRepeatSequence(s: string, threshold: number): boolean {
  let run = 1;
  for (let i = 1; i < s.length; i++) {
    run = s[i] === s[i - 1] ? run + 1 : 1;
    if (run >= threshold) return true;
  }
  return false;
}

// ίδια λογική με backend: συνεχόμενοι ASCII χαρακτήρες +1
function hasAscendingSequence(s: string, threshold: number): boolean {
  let run = 1;
  for (let i = 1; i < s.length; i++) {
    const prev = s.charCodeAt(i - 1);
    const cur = s.charCodeAt(i);
    run = cur === prev + 1 ? run + 1 : 1;
    if (run >= threshold) return true;
  }
  return false;
}

function containsIgnoreCase(haystack: string, needle: string): boolean {
  return haystack.toLowerCase().includes(needle.toLowerCase());
}

function anyTokenContained(password: string, fullName: string): boolean {
  const tokens = fullName.toLowerCase().split(/\s+/);
  const p = password.toLowerCase();
  for (const token of tokens) {
    if (token.length >= 3 && p.includes(token)) return true;
  }
  return false;
}
