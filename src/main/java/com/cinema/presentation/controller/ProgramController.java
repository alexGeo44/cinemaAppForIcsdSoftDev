package com.cinema.presentation.controller;

import com.cinema.application.programs.AddProgrammerUseCase;
import com.cinema.application.programs.AddStaffUseCase;
import com.cinema.application.programs.ChangeProgramStateUseCase;
import com.cinema.application.programs.CreateProgramUseCase;
import com.cinema.application.programs.DeleteProgramUseCase;
import com.cinema.application.programs.SearchProgramsUseCase;
import com.cinema.application.programs.UpdateProgramUseCase;
import com.cinema.application.programs.ViewProgramUseCase;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.presentation.dto.requests.CreateProgramRequest;
import com.cinema.presentation.dto.requests.UpdateProgramRequest;
import com.cinema.presentation.dto.responses.ProgramResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // =======================
    // 🆕 CREATE PROGRAM
    // =======================
    @PostMapping
    public ResponseEntity<Void> create(
            @RequestParam("creatorId") Long creatorId,
            @RequestBody CreateProgramRequest request
    ) {
        createProgram.create(
                new UserId(creatorId),
                request.name(),
                request.description(),
                request.startDate(),
                request.endDate()
        );

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // =======================
    // ✏ UPDATE PROGRAM
    // =======================
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @RequestParam Long actorUserId,
            @RequestBody UpdateProgramRequest request
    ) {
        updateProgram.update(
                new UserId(actorUserId),
                new ProgramId(id),
                request.name(),
                request.description(),
                request.startDate(),
                request.endDate()
        );

        return ResponseEntity.ok().build();
    }

    // =======================
    // 🗑 DELETE PROGRAM
    // =======================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam Long actorUserId
    ) {
        deleteProgram.delete(
                new UserId(actorUserId),
                new ProgramId(id)
        );
        return ResponseEntity.noContent().build();
    }

    // =======================
    // 👁 VIEW PROGRAM
    // =======================
    @GetMapping("/{id}")
    public ResponseEntity<ProgramResponse> view(@PathVariable Long id) {
        Program program = viewProgram.view(new ProgramId(id));
        ProgramResponse dto = toDto(program);
        return ResponseEntity.ok(dto);
    }

    // =======================
    // 🔍 SEARCH PROGRAMS
    // =======================
    @GetMapping
    public ResponseEntity<List<ProgramResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ProgramState programState,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "50") int limit
    ) {
        var result = searchPrograms.search(
                name,
                programState,
                from,
                to,
                offset,
                limit
        );

        var dtoList = result.stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    // =======================
    // 👨‍💻 ADD PROGRAMMER
    // =======================
    @PostMapping("/{id}/programmers/{userId}")
    public ResponseEntity<Void> addProgrammer(
            @PathVariable Long id,            // program id
            @PathVariable Long userId,        // user που γίνεται programmer
            @RequestParam Long actorUserId    // ποιος κάνει την ενέργεια
    ) {
        addProgrammer.addProgrammer(
                new UserId(actorUserId),
                new ProgramId(id),
                new UserId(userId)
        );
        return ResponseEntity.ok().build();
    }

    // =======================
    // 👷 ADD STAFF
    // =======================
    @PostMapping("/{id}/staff/{userId}")
    public ResponseEntity<Void> addStaff(
            @PathVariable Long id,            // program id
            @PathVariable Long userId,        // user που γίνεται staff
            @RequestParam Long actorUserId    // ποιος κάνει την ενέργεια
    ) {
        addStaff.addStaff(
                new UserId(actorUserId),
                new ProgramId(id),
                new UserId(userId)
        );
        return ResponseEntity.ok().build();
    }

    // =======================
    // 🔄 CHANGE STATE
    // =======================
    @PutMapping("/{id}/state")
    public ResponseEntity<Void> changeState(
            @PathVariable Long id,
            @RequestParam ProgramState newState,
            @RequestParam Long actorUserId
    ) {
        changeState.changeState(
                new UserId(actorUserId),
                new ProgramId(id),
                newState
        );

        return ResponseEntity.ok().build();
    }

    // =======================
    // MAPPING: Program -> DTO
    // =======================
    private ProgramResponse toDto(Program p) {
        return new ProgramResponse(
                p.id().value(),
                p.name(),
                p.description(),
                p.startDate(),
                p.endDate(),
                p.state().name()
        );
    }
}
