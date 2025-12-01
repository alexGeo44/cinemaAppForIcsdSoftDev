package application.screenings;

import domain.Exceptions.AuthorizationException;
import domain.Exceptions.NotFoundException;
import domain.entity.Screening;
import domain.entity.value.ProgramId;
import domain.entity.value.ScreeningId;
import domain.entity.value.UserId;
import domain.port.ProgramRepository;
import domain.port.ScreeningRepository;

public final class AssignHandlerUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public AssignHandlerUseCase(ScreeningRepository screeningRepository , ProgramRepository programRepository){
        this.screeningRepository = screeningRepository;
        this.programRepository = programRepository;
    }

    public void assignHandler(
            UserId callerId,
            ScreeningId screeningId,
            UserId staffId
    ){
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(()-> new NotFoundException("Screening", "Screening not found"));

        ProgramId programId = screening.programId();

        if(!programRepository.isProgrammer(programId , callerId)){ throw new AuthorizationException("Only PROGRAMMER of the program can assign staff"); }
        if(!programRepository.isStaff(programId, staffId)){ throw new AuthorizationException("User is not STAFF in this program"); }

        screening.assignStaff(staffId);
        screeningRepository.save(screening);
    }

}
