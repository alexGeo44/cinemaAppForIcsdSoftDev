package presentation.controller;

import application.screenings.*;
import domain.entity.value.ProgramId;
import domain.entity.value.ScreeningId;
import domain.entity.value.UserId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import presentation.dto.requests.CreateScreeningRequest;
import presentation.dto.requests.UpdateScreeningRequest;

import java.time.LocalDate;

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

    @PostMapping
    public ResponseEntity<Void> create(@RequestParam Long userId,
                                        @RequestParam Long programId,
                                        @RequestBody CreateScreeningRequest request) {
        create.create(new UserId(userId),new ProgramId(programId),request.title(),request.genre(),request.description());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @RequestParam Long callerId,
            @PathVariable Long screeningId,
            @RequestBody UpdateScreeningRequest request
    ) {
        update.update(new UserId(callerId),new ScreeningId(screeningId), request.title(),request.genre(), request.description());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/submit")
    public ResponseEntity<Void> submit(@RequestParam Long callerId,@PathVariable Long id) {
        submit.submit(new UserId(callerId),new ScreeningId(id));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/withdraw")
    public ResponseEntity<Void> withdraw(@RequestParam Long userId,
                                         @PathVariable Long id) {
        withdraw.withdraw(new UserId(userId),new ScreeningId(id));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/assign/{staffId}")
    public ResponseEntity<Void> assign(@RequestParam Long callerId,
                                       @PathVariable Long id,
                                       @RequestParam Long staffId) {
        assign.assignHandler(new UserId(callerId),new ScreeningId(id),new UserId(staffId));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<Void> accept(@RequestParam Long programmerId,
                                       @PathVariable Long id,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                       @RequestParam String room) {
        accept.acceptAndSchedule(new UserId(programmerId),new ScreeningId(id),date,room);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long staffId,@RequestParam Long id) {
        reject.reject(new UserId(staffId),new ScreeningId(id));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> view(@PathVariable Long id) {
        return ResponseEntity.ok(view.view(new ScreeningId(id)));
    }
}
