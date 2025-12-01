package application.screenings;

import domain.Exceptions.NotFoundException;
import domain.entity.Screening;
import domain.entity.value.ProgramId;
import domain.entity.value.ScreeningId;
import domain.entity.value.UserId;
import domain.enums.ScreeningState;
import domain.port.ProgramRepository;
import domain.port.ScreeningRepository;

import java.util.UUID;

public final class CreateScreeningUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public CreateScreeningUseCase(ScreeningRepository screeningRepository , ProgramRepository programRepository){
        this.screeningRepository = screeningRepository;
        this.programRepository = programRepository;
    }

    public Screening create(
            UserId submitterId,
            ProgramId programId,
            String title,
            String genre,
            String description
    ){
        programRepository.findById(programId)
                .orElseThrow(()-> new NotFoundException("Program", "Program not found"));

        Screening screening = new Screening(
                new ScreeningId(UUID.randomUUID().clockSequence()),
                programId,
                submitterId,
                title,
                genre,
                description,
                ScreeningState.CREATED
        );

        return screeningRepository.save(screening);
    }


}
