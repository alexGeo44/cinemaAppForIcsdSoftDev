package com.cinema.application.screenings;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.enums.ScreeningState;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.ScreeningRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

@Service
public class SearchScreeningsUseCase {

    private static final int MAX_LIMIT = 200;
    private static final int FETCH_PADDING = 200; // τραβάμε λίγο παραπάνω για να μην "κόβονται" αποτελέσματα μετά το filter

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public SearchScreeningsUseCase(ScreeningRepository screeningRepository,
                                   ProgramRepository programRepository) {
        this.screeningRepository = Objects.requireNonNull(screeningRepository);
        this.programRepository = Objects.requireNonNull(programRepository);
    }

    /**
     * Spec: Search screenings within a program by film fields + date-range with AND semantics.
     * NOTE: your domain currently supports title/genre/scheduledTime only.
     */
    @Transactional(readOnly = true)
    public List<Screening> searchInProgram(
            UserId actorId,
            ProgramId programId,
            String titleQuery,
            String genreQuery,
            LocalDate fromDate,
            LocalDate toDate,
            ScreeningState stateFilter, // optional
            int offset,
            int limit,
            boolean timetableSort
    ) {
        if (programId == null) throw new ValidationException("programId", "programId is required");

        int safeOffset = Math.max(0, offset);
        int safeLimit = Math.max(1, Math.min(limit, MAX_LIMIT));

        if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
            throw new ValidationException("dates", "toDate must be on/after fromDate");
        }

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // 1) fetch a window (not full table)
        int fetchOffset = Math.max(0, safeOffset - FETCH_PADDING);
        int fetchLimit = safeLimit + 2 * FETCH_PADDING;

        List<Screening> page = (stateFilter == null)
                ? screeningRepository.findByProgram(programId, fetchOffset, fetchLimit) // all states
                : screeningRepository.findByProgram(programId, stateFilter, fetchOffset, fetchLimit);

        Stream<Screening> stream = page.stream();

        // 2) role-aware access filter FIRST
        stream = stream.filter(s -> canView(actorId, programId, program, s));

        // 3) AND semantics filters
        if (hasText(titleQuery)) {
            var words = tokenize(titleQuery);
            stream = stream.filter(s -> containsAllWords(s.title(), words));
        }

        if (hasText(genreQuery)) {
            var words = tokenize(genreQuery);
            stream = stream.filter(s -> containsAllWords(s.genre(), words));
        }

        if (fromDate != null) {
            stream = stream.filter(s -> s.scheduledTime() != null && !s.scheduledTime().isBefore(fromDate));
        }
        if (toDate != null) {
            stream = stream.filter(s -> s.scheduledTime() != null && !s.scheduledTime().isAfter(toDate));
        }

        // 4) sorting
        Comparator<Screening> cmp = timetableSort
                ? Comparator
                .comparing((Screening s) -> s.scheduledTime() != null ? s.scheduledTime() : LocalDate.MIN)
                .thenComparing(s -> safeLower(s.title()))
                : Comparator
                .comparing((Screening s) -> safeLower(s.genre()))
                .thenComparing(s -> safeLower(s.title()));

        List<Screening> filteredSorted = stream.sorted(cmp).toList();

        // 5) paging AFTER filtering
        if (safeOffset >= filteredSorted.size()) return List.of();
        int toIndex = Math.min(filteredSorted.size(), safeOffset + safeLimit);
        return filteredSorted.subList(safeOffset, toIndex);
    }

    /**
     * SUBMITTER list: "my screenings"
     */
    @Transactional(readOnly = true)
    public List<Screening> myScreenings(UserId actorId, ScreeningState state, int offset, int limit) {
        if (actorId == null) throw new AuthorizationException("Unauthorized");

        int safeOffset = Math.max(0, offset);
        int safeLimit = Math.max(1, Math.min(limit, MAX_LIMIT));

        int fetchOffset = Math.max(0, safeOffset - FETCH_PADDING);
        int fetchLimit = safeLimit + 2 * FETCH_PADDING;

        List<Screening> raw = (state == null)
                ? screeningRepository.findBySubmitter(actorId, fetchOffset, fetchLimit) // all states
                : screeningRepository.findBySubmitter(actorId, state, fetchOffset, fetchLimit);

        List<Screening> sorted = raw.stream().sorted(byGenreThenTitle()).toList();

        if (safeOffset >= sorted.size()) return List.of();
        return sorted.subList(safeOffset, Math.min(sorted.size(), safeOffset + safeLimit));
    }

    /**
     * STAFF list: assigned only
     */
    @Transactional(readOnly = true)
    public List<Screening> myAssignedAsStaff(UserId staffId, int offset, int limit) {
        if (staffId == null) throw new AuthorizationException("Unauthorized");

        int safeOffset = Math.max(0, offset);
        int safeLimit = Math.max(1, Math.min(limit, MAX_LIMIT));

        int fetchOffset = Math.max(0, safeOffset - FETCH_PADDING);
        int fetchLimit = safeLimit + 2 * FETCH_PADDING;

        List<Screening> raw = screeningRepository.findByStaffMember(staffId, fetchOffset, fetchLimit);

        List<Screening> sorted = raw.stream().sorted(byGenreThenTitle()).toList();

        if (safeOffset >= sorted.size()) return List.of();
        return sorted.subList(safeOffset, Math.min(sorted.size(), safeOffset + safeLimit));
    }

    private boolean canView(UserId actorId, ProgramId programId, Program program, Screening screening) {
        boolean isPublic = program.state() == ProgramState.ANNOUNCED
                && screening.state() == ScreeningState.SCHEDULED;

        if (actorId == null) return isPublic;

        if (programRepository.isProgrammer(programId, actorId)) return true;
        if (screening.isOwner(actorId)) return true;

        if (screening.isAssignedTo(actorId) && programRepository.isStaff(programId, actorId)) return true;

        return isPublic;
    }

    private Comparator<Screening> byGenreThenTitle() {
        return Comparator.comparing((Screening s) -> safeLower(s.genre()))
                .thenComparing(s -> safeLower(s.title()));
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private String safeLower(String s) {
        return (s == null) ? "" : s.toLowerCase(Locale.ROOT);
    }

    private List<String> tokenize(String q) {
        String norm = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        if (norm.isEmpty()) return List.of();
        return Arrays.stream(norm.split("\\s+"))
                .filter(w -> !w.isBlank())
                .toList();
    }

    private boolean containsAllWords(String field, List<String> words) {
        if (words == null || words.isEmpty()) return true;
        if (field == null) return false;
        String hay = field.toLowerCase(Locale.ROOT);
        for (String w : words) {
            if (!hay.contains(w)) return false;
        }
        return true;
    }
}
