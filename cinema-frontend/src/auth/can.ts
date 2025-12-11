import { useAuthStore } from "./auth.store";
import { BaseRole } from "@/domain/auth/auth.types";

export function canManageUsers() {
    const auth = useAuthStore();
    return auth.is(BaseRole.ADMIN);
}

export function canManagePrograms() {
    const auth = useAuthStore();
    return auth.is([BaseRole.ADMIN, BaseRole.PROGRAMMER]);
}

export function canReviewScreenings() {
    const auth = useAuthStore();
    return auth.is([BaseRole.ADMIN, BaseRole.STAFF]);
}

export function canSubmitScreenings() {
    const auth = useAuthStore();
    return auth.is(BaseRole.SUBMITTER);
}
