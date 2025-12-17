package com.cinema.domain.entity.value;

import java.io.Serializable;

public record UserId(Long value) implements Serializable {
    public UserId{
        if( value <= 0) throw new IllegalArgumentException("User ID must be positive");

    }
    public static UserId of(long value){ return new UserId(value);  }
}
