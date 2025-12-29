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

    /**
     * Safe actor: returns null for VISITOR.
     */
    private UserId actorOrNull(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) return null;
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

    private ProgramPublicResponse toPublicDto(Program p) {
        return new ProgramPublicResponse(
                p.id() != null ? p.id().value() : null,
                p.name(),
                p.description(),
                p.startDate(),
                p.endDate(),
                p.state().name(),
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

    /**
     * Role-aware response:
     * - If use case returns a Program that the actor is allowed to see fully -> full DTO
     * - Otherwise -> public DTO
     *
     * IMPORTANT: The actual access decision should be enforced in ViewProgramUseCase/SearchProgramsUseCase.
     */
    private ProgramViewResponse toRoleAwareDto(Program p, boolean full) {
        return full ? toFullDto(p) : toPublicDto(p);
    }

    // -------------------------
    // endpoints
    // -------------------------

    @PostMapping
    public ResponseEntity<Void> create(Authentication auth, @RequestBody CreateProgramRequest request) {
        createProgram.create(
                requireActor(auth),
                request.name(),
                request.description(),
                request.startDate(),
                request.endDate()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(Authentication auth, @PathVariable Long id, @RequestBody UpdateProgramRequest request) {
        updateProgram.update(
                requireActor(auth),
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
        deleteProgram.delete(requireActor(auth), new ProgramId(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgramViewResponse> view(Authentication auth, @PathVariable Long id) {
        UserId actorId = actorOrNull(auth);

        // ViewProgramUseCase should decide if actor can see full details or only public.
        ViewProgramUseCase.ViewResult result = viewProgram.view(actorId, new ProgramId(id));

        return ResponseEntity.ok(toRoleAwareDto(result.program(), result.full()));
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
        UserId actorId = actorOrNull(auth);

        var result = searchPrograms.search(actorId, name, programState, from, to, offset, limit);

        // For each program, decide if actor gets full or public view.
        // Best: SearchProgramsUseCase can also return a "full/public" flag per item.
        var dtoList = result.stream()
                .map(p -> toRoleAwareDto(p, viewProgram.canViewFull(actorId, p)))
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/{id}/programmers/{userId}")
    public ResponseEntity<Void> addProgrammer(Authentication auth, @PathVariable Long id, @PathVariable Long userId) {
        addProgrammer.addProgrammer(requireActor(auth), new ProgramId(id), new UserId(userId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/staff/{userId}")
    public ResponseEntity<Void> addStaff(Authentication auth, @PathVariable Long id, @PathVariable Long userId) {
        addStaff.addStaff(requireActor(auth), new ProgramId(id), new UserId(userId));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/state")
    public ResponseEntity<ProgramResponse> changeState(Authentication auth, @PathVariable Long id, @RequestBody ChangeProgramStateRequest request) {
        ProgramState next;
        try {
            next = ProgramState.valueOf(request.nextState());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid nextState");
        }

        Program updated = changeState.changeState(requireActor(auth), new ProgramId(id), next);
        return ResponseEntity.ok(toFullDto(updated));
    }
}
