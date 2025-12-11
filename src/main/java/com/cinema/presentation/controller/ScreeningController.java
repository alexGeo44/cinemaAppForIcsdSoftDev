package com.cinema.presentation.controller;

import com.cinema.application.screenings.*;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ScreeningState;
import com.cinema.presentation.dto.requests.CreateScreeningRequest;
import com.cinema.presentation.dto.requests.UpdateScreeningRequest;
import com.cinema.presentation.dto.responses.ScreeningResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
    private final AssignHandlerUseCase assign;
    private final AcceptScreeningUseCase accept;
    private final RejectScreeningUseCase reject;
    private final ViewScreeningUseCase view;
    private final SearchScreeningsUseCase search;

    public ScreeningController(
            CreateScreeningUseCase create,
            UpdateScreeningUseCase update,
            SubmitScreeningUseCase submit,
            WithdrawScreeningUseCase withdraw,
            AssignHandlerUseCase assign,
            AcceptScreeningUseCase accept,
            RejectScreeningUseCase reject,
            ViewScreeningUseCase view,
            SearchScreeningsUseCase search
    ) {
        this.create = create;
        this.update = update;
        this.submit = submit;
        this.withdraw = withdraw;
        this.assign = assign;
        this.accept = accept;
        this.reject = reject;
        this.view = view;
        this.search = search;
    }

    // ---------- helpers ----------

    private ScreeningResponse toDto(Screening s) {
        return new ScreeningResponse(
                s.id().value(),
                s.programId().value(),
                s.submitterId().value(),
                s.title(),
                s.genre(),
                s.description(),
                s.room(),
                s.scheduledTime(),
                s.state().name(),                         // String state
                s.staffMemberId() != null ? s.staffMemberId().value() : null,
                s.submittedTime(),
                s.reviewedTime()
        );
    }

    private int orDefault(Integer value, int def) {
        return value != null ? value : def;
    }

    private ScreeningState parseState(String state) {
        if (state == null || state.isBlank()) {
            return null;
        }
        return ScreeningState.valueOf(state);
    }

    // ---------- CREATE ----------

    // POST /api/screenings?userId=&programId=
    @PostMapping
    public ResponseEntity<Void> create(
            @RequestParam Long userId,
            @RequestParam Long programId,
            @RequestBody CreateScreeningRequest request
    ) {
        create.create(
                new UserId(userId),
                new ProgramId(programId),
                request.title(),
                request.genre(),
                request.description()
        );
        return ResponseEntity.ok().build();
    }

    // ---------- UPDATE ----------

    // PUT /api/screenings/{id}?callerId=
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @RequestParam Long callerId,
            @PathVariable("id") Long screeningId,
            @RequestBody UpdateScreeningRequest request
    ) {
        update.update(
                new UserId(callerId),
                new ScreeningId(screeningId),
                request.title(),
                request.genre(),
                request.description()
        );
        return ResponseEntity.ok().build();
    }

    // ---------- SUBMIT ----------

    // PUT /api/screenings/{id}/submit?callerId=
    @PutMapping("/{id}/submit")
    public ResponseEntity<Void> submit(
            @RequestParam Long callerId,
            @PathVariable Long id
    ) {
        submit.submit(new UserId(callerId), new ScreeningId(id));
        return ResponseEntity.ok().build();
    }

    // ---------- WITHDRAW ----------

    // PUT /api/screenings/{id}/withdraw?userId=
    @PutMapping("/{id}/withdraw")
    public ResponseEntity<Void> withdraw(
            @RequestParam Long userId,
            @PathVariable Long id
    ) {
        withdraw.withdraw(new UserId(userId), new ScreeningId(id));
        return ResponseEntity.ok().build();
    }

    // ---------- ASSIGN STAFF ----------

    // PUT /api/screenings/{id}/assign/{staffId}?callerId=
    @PutMapping("/{id}/assign/{staffId}")
    public ResponseEntity<Void> assign(
            @RequestParam Long callerId,
            @PathVariable Long id,
            @PathVariable Long staffId
    ) {
        assign.assignHandler(
                new UserId(callerId),
                new ScreeningId(id),
                new UserId(staffId)
        );
        return ResponseEntity.ok().build();
    }

    // ---------- ACCEPT & SCHEDULE ----------

    // PUT /api/screenings/{id}/accept?programmerId=&date=&room=
    @PutMapping("/{id}/accept")
    public ResponseEntity<Void> accept(
            @RequestParam Long programmerId,
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String room
    ) {
        accept.acceptAndSchedule(
                new UserId(programmerId),
                new ScreeningId(id),
                date,
                room
        );
        return ResponseEntity.ok().build();
    }

    // ---------- REJECT ----------

    // PUT /api/screenings/{id}/reject?staffId=
    @PutMapping("/{id}/reject")
    public ResponseEntity<Void> reject(
            @RequestParam Long staffId,
            @PathVariable Long id
    ) {
        reject.reject(new UserId(staffId), new ScreeningId(id));
        return ResponseEntity.ok().build();
    }

    // ---------- VIEW SINGLE ----------

    // GET /api/screenings/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ScreeningResponse> view(@PathVariable Long id) {
        Screening s = view.view(new ScreeningId(id));
        return ResponseEntity.ok(toDto(s));
    }

    // ---------- LISTS ----------

    // GET /api/screenings/by-program
    @GetMapping("/by-program")
    public ResponseEntity<List<ScreeningResponse>> byProgram(
            @RequestParam Long programId,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit
    ) {
        var result = search.byProgram(
                new ProgramId(programId),
                parseState(state),
                orDefault(offset, 0),
                orDefault(limit, 50)
        );

        var dto = result.stream().map(this::toDto).toList();
        return ResponseEntity.ok(dto);
    }

    // GET /api/screenings/by-submitter
    @GetMapping("/by-submitter")
    public ResponseEntity<List<ScreeningResponse>> bySubmitter(
            @RequestParam Long submitterId,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit
    ) {
        var result = search.bySubmitter(
                new UserId(submitterId),
                parseState(state),
                orDefault(offset, 0),
                orDefault(limit, 50)
        );

        var dto = result.stream().map(this::toDto).toList();
        return ResponseEntity.ok(dto);
    }

    // GET /api/screenings/by-staff
    @GetMapping("/by-staff")
    public ResponseEntity<List<ScreeningResponse>> byStaff(
            @RequestParam Long staffId,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit
    ) {
        var result = search.byAssignedStaff(
                new UserId(staffId),
                orDefault(offset, 0),
                orDefault(limit, 50)
        );

        var dto = result.stream().map(this::toDto).toList();
        return ResponseEntity.ok(dto);
    }
}
