package com.cinema.presentation.controller;

import com.cinema.application.screenings.*;
import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ScreeningState;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.presentation.dto.requests.CreateScreeningRequest;
import com.cinema.presentation.dto.requests.UpdateScreeningRequest;
import com.cinema.presentation.dto.responses.ScreeningPublicResponse;
import com.cinema.presentation.dto.responses.ScreeningResponse;
import com.cinema.presentation.dto.responses.ScreeningViewResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/screenings")
public class ScreeningController {

    private final CreateScreeningUseCase create;
    private final UpdateScreeningUseCase update;
    private final SubmitScreeningUseCase submit;
    private final WithdrawScreeningUseCase withdraw;
    private final AssignHandlerUseCase assignHandler;
    private final ReviewScreeningUseCase review;
    private final ApproveScreeningUseCase approve;
    private final FinalSubmitScreeningUseCase finalSubmit;
    private final RejectScreeningUseCase reject;
    private final ScheduleScreeningUseCase schedule;
    private final ViewScreeningUseCase view;
    private final SearchScreeningsUseCase search;

    // ✅ needed for role-aware mapping in /by-program without N+1
    private final ProgramRepository programRepository;

    public ScreeningController(
            CreateScreeningUseCase create,
            UpdateScreeningUseCase update,
            SubmitScreeningUseCase submit,
            WithdrawScreeningUseCase withdraw,
            AssignHandlerUseCase assignHandler,
            ReviewScreeningUseCase review,
            ApproveScreeningUseCase approve,
            FinalSubmitScreeningUseCase finalSubmit,
            RejectScreeningUseCase reject,
            ScheduleScreeningUseCase schedule,
            ViewScreeningUseCase view,
            SearchScreeningsUseCase search,
            ProgramRepository programRepository
    ) {
        this.create = Objects.requireNonNull(create);
        this.update = Objects.requireNonNull(update);
        this.submit = Objects.requireNonNull(submit);
        this.withdraw = Objects.requireNonNull(withdraw);
        this.assignHandler = Objects.requireNonNull(assignHandler);
        this.review = Objects.requireNonNull(review);
        this.approve = Objects.requireNonNull(approve);
        this.finalSubmit = Objects.requireNonNull(finalSubmit);
        this.reject = Objects.requireNonNull(reject);
        this.schedule = Objects.requireNonNull(schedule);
        this.view = Objects.requireNonNull(view);
        this.search = Objects.requireNonNull(search);
        this.programRepository = Objects.requireNonNull(programRepository);
    }

    // -------------------------
    // auth helpers
    // -------------------------

    private UserId actorOrNull(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) return null; // VISITOR
        Object p = auth.getPrincipal();
        if (p instanceof Long l) return new UserId(l);
        if (p instanceof Integer i) return new UserId(i.longValue());
        try {
            return new UserId(Long.parseLong(String.valueOf(p)));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private UserId requireActor(Authentication auth) {
        UserId id = actorOrNull(auth);
        if (id == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        return id;
    }

    private int orDefault(Integer value, int def) {
        return value != null ? value : def;
    }

    private ScreeningState parseStateOrNull(String state) {
        if (state == null || state.isBlank()) return null;
        try {
            return ScreeningState.valueOf(state);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid state");
        }
    }

    // -------------------------
    // DTO mapping
    // -------------------------

    private ScreeningPublicResponse toPublicDto(Screening s) {
        return new ScreeningPublicResponse(
                s.id() != null ? s.id().value() : null,
                s.programId().value(),
                s.title(),
                s.genre(),
                s.scheduledTime(),
                s.room()
        );
    }

    private ScreeningResponse toFullDto(Screening s) {
        return new ScreeningResponse(
                s.id() != null ? s.id().value() : null,
                s.programId().value(),
                s.submitterId().value(),
                s.title(),
                s.genre(),
                s.description(),
                s.room(),
                s.scheduledTime(),
                s.state().name(),
                s.staffMemberId() != null ? s.staffMemberId().value() : null,
                s.submittedTime(),
                s.reviewedTime()
        );
    }

    private ScreeningViewResponse toRoleAwareDto(Screening s, boolean full) {
        return full ? toFullDto(s) : toPublicDto(s);
    }

    // =========================
    // Commands
    // =========================

    @PostMapping
    public ResponseEntity<ScreeningResponse> create(
            Authentication auth,
            @RequestParam Long programId,
            @RequestBody CreateScreeningRequest request
    ) {
        Screening s = create.create(
                requireActor(auth),
                new ProgramId(programId),
                request.title(),
                request.genre(),
                request.description()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toFullDto(s));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            Authentication auth,
            @PathVariable("id") Long screeningId,
            @RequestBody UpdateScreeningRequest request
    ) {
        update.update(
                requireActor(auth),
                new ScreeningId(screeningId),
                request.title(),
                request.genre(),
                request.description()
        );
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/submit")
    public ResponseEntity<Void> submit(Authentication auth, @PathVariable Long id) {
        submit.submit(requireActor(auth), new ScreeningId(id));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/withdraw")
    public ResponseEntity<Void> withdraw(Authentication auth, @PathVariable Long id) {
        withdraw.withdraw(requireActor(auth), new ScreeningId(id));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/handler/{staffId}")
    public ResponseEntity<Void> assignHandler(
            Authentication auth,
            @PathVariable Long id,
            @PathVariable Long staffId
    ) {
        assignHandler.assignHandler(
                requireActor(auth),
                new ScreeningId(id),
                new UserId(staffId)
        );
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<Void> review(
            Authentication auth,
            @PathVariable Long id,
            @RequestParam int score,
            @RequestParam(required = false) String comments
    ) {
        review.review(requireActor(auth), new ScreeningId(id), score, comments);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Void> approve(Authentication auth, @PathVariable Long id) {
        approve.approve(requireActor(auth), new ScreeningId(id));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/final-submit")
    public ResponseEntity<Void> finalSubmit(Authentication auth, @PathVariable Long id) {
        finalSubmit.finalSubmit(requireActor(auth), new ScreeningId(id));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Void> reject(
            Authentication auth,
            @PathVariable Long id,
            @RequestParam String reason
    ) {
        reject.reject(requireActor(auth), new ScreeningId(id), reason);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/schedule")
    public ResponseEntity<Void> schedule(
            Authentication auth,
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String room
    ) {
        schedule.schedule(requireActor(auth), new ScreeningId(id), date, room);
        return ResponseEntity.ok().build();
    }

    // =========================
    // Queries
    // =========================

    @GetMapping("/{id}")
    public ResponseEntity<ScreeningViewResponse> view(Authentication auth, @PathVariable Long id) {
        UserId actorId = actorOrNull(auth); // VISITOR allowed
        var result = view.view(actorId, new ScreeningId(id));
        return ResponseEntity.ok(toRoleAwareDto(result.screening(), result.full()));
    }

    /**
     * Spec-like search (AND semantics + words-in-field) inside a program.
     * NOTE: your domain model currently supports title/genre/scheduledTime only.
     */
    @GetMapping("/by-program")
    public ResponseEntity<List<ScreeningViewResponse>> byProgram(
            Authentication auth,
            @RequestParam Long programId,

            // filters
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String state,

            // paging
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit,

            // sorting view
            @RequestParam(defaultValue = "false") boolean timetable
    ) {
        UserId actorId = actorOrNull(auth);
        ProgramId pid = new ProgramId(programId);

        List<Screening> screenings = search.searchInProgram(
                actorId,
                pid,
                title,
                genre,
                from,
                to,
                parseStateOrNull(state),
                orDefault(offset, 0),
                orDefault(limit, 50),
                timetable
        );

        // ✅ one program fetch, no N+1
        Program program = programRepository.findById(pid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Program not found"));

        var dtoList = screenings.stream()
                .map(s -> {
                    boolean full = view.canViewFull(actorId, program, s);
                    return toRoleAwareDto(s, full);
                })
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    /**
     * Spec-wise: "my screenings" only.
     * If you still want submitterId param for UI, enforce submitterId == actorId.
     */
    @GetMapping("/by-submitter")
    public ResponseEntity<List<ScreeningResponse>> bySubmitter(
            Authentication auth,
            @RequestParam(required = false) Long submitterId,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit
    ) {
        UserId actorId = requireActor(auth);

        if (submitterId != null && !actorId.equals(new UserId(submitterId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the submitter can list their own screenings");
        }

        var result = search.myScreenings(
                actorId,
                parseStateOrNull(state),
                orDefault(offset, 0),
                orDefault(limit, 50)
        );

        return ResponseEntity.ok(result.stream().map(this::toFullDto).toList());
    }

    @GetMapping("/by-staff")
    public ResponseEntity<List<ScreeningResponse>> byStaff(
            Authentication auth,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit
    ) {
        UserId staffId = requireActor(auth);

        // if you want a strict STAFF-only check, do it in use case (recommended)
        try {
            var result = search.myAssignedAsStaff(
                    staffId,
                    orDefault(offset, 0),
                    orDefault(limit, 50)
            );
            return ResponseEntity.ok(result.stream().map(this::toFullDto).toList());
        } catch (AuthorizationException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage());
        }
    }
}
