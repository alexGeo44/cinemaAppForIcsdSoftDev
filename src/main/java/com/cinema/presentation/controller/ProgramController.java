package com.cinema.presentation.controller;

import com.cinema.application.programs.*;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.presentation.dto.requests.ChangeProgramStateRequest;
import com.cinema.presentation.dto.requests.CreateProgramRequest;
import com.cinema.presentation.dto.requests.UpdateProgramRequest;
import com.cinema.presentation.dto.responses.ProgramPublicResponse;
import com.cinema.presentation.dto.responses.ProgramResponse;
import com.cinema.presentation.dto.responses.ProgramViewResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/programs")
public class ProgramController {

    private final CreateProgramUseCase createProgram;
    private final UpdateProgramUseCase updateProgram;
    private final DeleteProgramUseCase deleteProgram;
    private final ViewProgramUseCase viewProgram;
    private final SearchProgramsUseCase searchPrograms;
    private final AddProgrammerUseCase addProgrammer;
    private final AddStaffUseCase addStaff;
    private final ChangeProgramStateUseCase changeState;

    public ProgramController(
            CreateProgramUseCase createProgram,
            UpdateProgramUseCase updateProgram,
            DeleteProgramUseCase deleteProgram,
            ViewProgramUseCase viewProgram,
            SearchProgramsUseCase searchPrograms,
            AddProgrammerUseCase addProgrammer,
            AddStaffUseCase addStaff,
            ChangeProgramStateUseCase changeState
    ) {
        this.createProgram = createProgram;
        this.updateProgram = updateProgram;
        this.deleteProgram = deleteProgram;
        this.viewProgram = viewProgram;
        this.searchPrograms = searchPrograms;
        this.addProgrammer = addProgrammer;
        this.addStaff = addStaff;
        this.changeState = changeState;
    }

    private UserId actor(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        Object p = auth.getPrincipal();
        if (p instanceof Long l) return new UserId(l);
        if (p instanceof Integer i) return new UserId(i.longValue());
        return new UserId(Long.parseLong(String.valueOf(p)));
    }

    private boolean isPublic(Program p) {
        return p.state() == ProgramState.ANNOUNCED;
    }

    private ProgramPublicResponse toPublicDto(Program p) {
        return new ProgramPublicResponse(
                p.id() != null ? p.id().value() : null,
                p.name(),
                p.description(),
                p.startDate(),
                p.endDate(),
                ProgramState.ANNOUNCED.name(),
                p.programmers().stream().map(UserId::value).toList()
        );
    }

    private ProgramResponse toFullDto(Program p) {
        return new ProgramResponse(
                p.id() != null ? p.id().value() : null,
                p.name(),
                p.description(),
                p.startDate(),
                p.endDate(),
                p.state().name(),
                p.creatorUserId().value(),
                p.programmers().stream().map(UserId::value).toList(),
                p.staff().stream().map(UserId::value).toList()
        );
    }

    private ProgramViewResponse toRoleAwareDto(Program p) {
        return isPublic(p) ? toPublicDto(p) : toFullDto(p);
    }

    @PostMapping
    public ResponseEntity<Void> create(
            Authentication auth,
            @RequestBody CreateProgramRequest request
    ) {
        createProgram.create(
                actor(auth),
                request.name(),
                request.description(),
                request.startDate(),
                request.endDate()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            Authentication auth,
            @PathVariable Long id,
            @RequestBody UpdateProgramRequest request
    ) {
        updateProgram.update(
                actor(auth),
                new ProgramId(id),
                request.name(),
                request.description(),
                request.startDate(),
                request.endDate()
        );
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        deleteProgram.delete(actor(auth), new ProgramId(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgramViewResponse> view(Authentication auth, @PathVariable Long id) {
        UserId actorId = (auth != null) ? actor(auth) : null;
        Program program = viewProgram.view(actorId, new ProgramId(id));
        return ResponseEntity.ok(toRoleAwareDto(program));
    }

    @GetMapping
    public ResponseEntity<List<ProgramViewResponse>> search(
            Authentication auth,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ProgramState programState,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "50") int limit
    ) {
        UserId actorId = (auth != null) ? actor(auth) : null;

        var result = searchPrograms.search(actorId, name, programState, from, to, offset, limit);

        var dtoList = result.stream().map(this::toRoleAwareDto).toList();
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/{id}/programmers/{userId}")
    public ResponseEntity<Void> addProgrammer(
            Authentication auth,
            @PathVariable Long id,
            @PathVariable Long userId
    ) {
        addProgrammer.addProgrammer(actor(auth), new ProgramId(id), new UserId(userId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/staff/{userId}")
    public ResponseEntity<Void> addStaff(
            Authentication auth,
            @PathVariable Long id,
            @PathVariable Long userId
    ) {
        addStaff.addStaff(actor(auth), new ProgramId(id), new UserId(userId));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/state")
    public ResponseEntity<ProgramResponse> changeState(
            Authentication auth,
            @PathVariable Long id,
            @RequestBody ChangeProgramStateRequest request
    ) {
        ProgramState next;
        try {
            next = ProgramState.valueOf(request.nextState());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }

        Program updated = changeState.changeState(actor(auth), new ProgramId(id), next);
        return ResponseEntity.ok(toFullDto(updated));
    }
}
