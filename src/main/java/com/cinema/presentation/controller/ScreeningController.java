package com.cinema.presentation.controller;

import com.cinema.application.screenings.*;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ScreeningState;
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

import java.time.LocalDate;
import java.util.List;

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
            SearchScreeningsUseCase search
    ) {
        this.create = create;
        this.update = update;
        this.submit = submit;
        this.withdraw = withdraw;
        this.assignHandler = assignHandler;
        this.review = review;
        this.approve = approve;
        this.finalSubmit = finalSubmit;
        this.reject = reject;
        this.schedule = schedule;
        this.view = view;
        this.search = search;
    }

    private UserId actor(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) return null;
        Object p = auth.getPrincipal();
        if (p instanceof Long l) return new UserId(l);
        if (p instanceof Integer i) return new UserId(i.longValue());
        return new UserId(Long.parseLong(String.valueOf(p)));
    }

    private boolean isPublic(Screening s) {
        return s.state() == ScreeningState.SCHEDULED;
    }

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

    private ScreeningViewResponse toRoleAwareDto(Screening s) {
        return isPublic(s) ? toPublicDto(s) : toFullDto(s);
    }

    private int orDefault(Integer value, int def) {
        return value != null ? value : def;
    }

    private ScreeningState parseStateOrNull(String state) {
        if (state == null || state.isBlank()) return null;
        return ScreeningState.valueOf(state);
    }

    @PostMapping
    public ResponseEntity<ScreeningResponse> create(
            Authentication auth,
            @RequestParam Long programId,
            @RequestBody CreateScreeningRequest request
    ) {
        Screening s = create.create(
                actor(auth),
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
                actor(auth),
                new ScreeningId(screeningId),
                request.title(),
                request.genre(),
                request.description()
        );
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/submit")
    public ResponseEntity<Void> submit(Authentication auth, @PathVariable Long id) {
        submit.submit(actor(auth), new ScreeningId(id));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/withdraw")
    public ResponseEntity<Void> withdraw(Authentication auth, @PathVariable Long id) {
        withdraw.withdraw(actor(auth), new ScreeningId(id));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/handler/{staffId}")
    public ResponseEntity<Void> assignHandler(
            Authentication auth,
            @PathVariable Long id,
            @PathVariable Long staffId
    ) {
        assignHandler.assignHandler(
                actor(auth),
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
        review.review(actor(auth), new ScreeningId(id), score, comments);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Void> approve(Authentication auth, @PathVariable Long id) {
        approve.approve(actor(auth), new ScreeningId(id));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/final-submit")
    public ResponseEntity<Void> finalSubmit(Authentication auth, @PathVariable Long id) {
        finalSubmit.finalSubmit(actor(auth), new ScreeningId(id));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Void> reject(
            Authentication auth,
            @PathVariable Long id,
            @RequestParam String reason
    ) {
        // ✅ απαιτεί RejectScreeningUseCase.reject(UserId, ScreeningId, String)
        reject.reject(actor(auth), new ScreeningId(id), reason);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/schedule")
    public ResponseEntity<Void> schedule(
            Authentication auth,
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String room
    ) {
        schedule.schedule(actor(auth), new ScreeningId(id), date, room);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScreeningViewResponse> view(
            Authentication auth,
            @PathVariable Long id
    ) {
        UserId actorId = actor(auth); // μπορεί να είναι null (visitor)
        Screening s = view.view(actorId, new ScreeningId(id));
        return ResponseEntity.ok(toRoleAwareDto(s));
    }

    @GetMapping("/by-program")
    public ResponseEntity<List<ScreeningViewResponse>> byProgram(
            Authentication auth,
            @RequestParam Long programId,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit
    ) {
        UserId actorId = actor(auth);

        var result = search.byProgram(
                actorId,
                new ProgramId(programId),
                parseStateOrNull(state),
                orDefault(offset, 0),
                orDefault(limit, 50)
        );

        return ResponseEntity.ok(result.stream().map(this::toRoleAwareDto).toList());
    }

    @GetMapping("/by-submitter")
    public ResponseEntity<List<ScreeningResponse>> bySubmitter(
            Authentication auth,
            @RequestParam Long submitterId,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit
    ) {
        UserId actorId = actor(auth);

        var result = search.bySubmitter(
                actorId,
                new UserId(submitterId),
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
        var result = search.byAssignedStaff(
                actor(auth),
                orDefault(offset, 0),
                orDefault(limit, 50)
        );

        return ResponseEntity.ok(result.stream().map(this::toFullDto).toList());
    }
}
