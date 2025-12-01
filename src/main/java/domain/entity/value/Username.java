package domain.entity.value;

import java.util.regex.Pattern;

public record Username(String value) {
    private static final Pattern PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]{4,19}$");

    public Username{
        if(value == null || value.isBlank()) throw new IllegalArgumentException("Username cannot be null");
        if(!PATTERN.matcher(value).matches()) throw new IllegalArgumentException("Username must start with a letter and be 5–20 characters long (letters, digits, underscores only)");

    }

    public String toString(){ return value; }

    public static Username of(String raw) {
        return new Username(raw);
    }
}
