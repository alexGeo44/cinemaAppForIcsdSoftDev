package domain.entity.value;

public record ProgramId(long value) {
    public ProgramId {
        if (value <= 0) throw new IllegalArgumentException("Program ID must be positive");
    }
}