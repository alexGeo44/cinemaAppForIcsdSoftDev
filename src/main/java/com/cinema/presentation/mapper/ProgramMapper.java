package com.cinema.presentation.mapper;

import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.presentation.dto.responses.ProgramPublicResponse;
import com.cinema.presentation.dto.responses.ProgramResponse;
import com.cinema.presentation.dto.responses.ProgramViewResponse;

import java.util.Collections;

public class ProgramMapper {

    private ProgramMapper() {}

    public static boolean isPublic(Program program) {
        return program != null && program.state() == ProgramState.ANNOUNCED;
    }

    public static ProgramPublicResponse toPublicResponse(Program program) {
        if (program == null) return null;

        return new ProgramPublicResponse(
                program.id() != null ? program.id().value() : null,
                program.name(),
                program.description(),
                program.startDate(),
                program.endDate(),
                ProgramState.ANNOUNCED.name(),
                program.programmers() != null
                        ? program.programmers().stream().map(UserId::value).toList()
                        : Collections.emptyList()
        );
    }

    public static ProgramResponse toFullResponse(Program program) {
        if (program == null) return null;

        return new ProgramResponse(
                program.id() != null ? program.id().value() : null,
                program.name(),
                program.description(),
                program.startDate(),
                program.endDate(),
                program.state() != null ? program.state().name() : null,
                program.creatorUserId() != null ? program.creatorUserId().value() : null,
                program.programmers() != null
                        ? program.programmers().stream().map(UserId::value).toList()
                        : Collections.emptyList(),
                program.staff() != null
                        ? program.staff().stream().map(UserId::value).toList()
                        : Collections.emptyList()
        );
    }

    public static ProgramViewResponse toRoleAwareResponse(Program program) {
        return isPublic(program) ? toPublicResponse(program) : toFullResponse(program);
    }
}
