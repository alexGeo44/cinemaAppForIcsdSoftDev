package com.cinema.application.screenings;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.enums.ScreeningState;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.ScreeningRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public  class SearchScreeningsUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public SearchScreeningsUseCase(ScreeningRepository screeningRepository,
                                   ProgramRepository programRepository) {
        this.screeningRepository = screeningRepository;
        this.programRepository = programRepository;
    }

    /**
     * Search screenings inside a program (role-aware).
     * actorId null => VISITOR
     */
    public List<Screening> byProgram(
            UserId actorId,
            ProgramId programId,
            ScreeningState state,
            int offset,
            int limit
    ) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        List<Screening> raw = screeningRepository.findByProgram(programId, state, offset, limit);

        return raw.stream()
                .filter(s -> canView(actorId, program, s))
                .sorted(byGenreThenTitle())
                .toList();
    }

    /**
     * Self list: επιτρέπεται να δεις submissions *του submitterId* μόνο αν είσαι ο ίδιος.
     * (Αν θες να επιτρέπεται και σε PROGRAMMER για τα programs του, το κάνουμε μετά.)
     */
    public List<Screening> bySubmitter(
            UserId actorId,
            UserId submitterId,
            ScreeningState state,
            int offset,
            int limit
    ) {
        if (actorId == null || !actorId.equals(submitterId)) {
            throw new AuthorizationException("Only the submitter can list their own screenings");
        }

        List<Screening> raw = screeningRepository.findBySubmitter(submitterId, state, offset, limit);
        return raw.stream()
                .sorted(byGenreThenTitle())
                .toList();
    }

    /**
     * STAFF list: βλέπει μόνο τα assigned.
     */
    public List<Screening> byAssignedStaff(
            UserId staffId,
            int offset,
            int limit
    ) {
        if (staffId == null) throw new AuthorizationException("Unauthorized");
        List<Screening> raw = screeningRepository.findByStaffMember(staffId, offset, limit);

        // assigned list είναι full by definition (έτσι κι αλλιώς είναι ήδη filtered)
        return raw.stream()
                .sorted(byGenreThenTitle())
                .toList();
    }

    private boolean canView(UserId actorId, Program program, Screening screening) {
        boolean isPublic = program.state() == ProgramState.ANNOUNCED
                && screening.state() == ScreeningState.SCHEDULED;

        if (actorId == null) return isPublic;

        if (program.isProgrammer(actorId)) return true;
        if (screening.isOwner(actorId)) return true;
        if (program.isStaff(actorId) && screening.isAssignedTo(actorId)) return true;

        return isPublic;
    }

    private Comparator<Screening> byGenreThenTitle() {
        return Comparator
                .comparing((Screening s) -> safeLower(s.genre()))
                .thenComparing(s -> safeLower(s.title()));
    }

    private String safeLower(String s) {
        return (s == null) ? "" : s.toLowerCase();
    }
}
