package domain.entity.value;

import org.springframework.security.crypto.bcrypt.BCrypt;


public record HashedPassword(String value) {

    public HashedPassword{
        if(value == null || value.isBlank()) throw new IllegalArgumentException("Password hash must not be null or blank");
    }

    public static HashedPassword fromRaw(String rawPassword){
        if(rawPassword == null || rawPassword.isBlank()) throw new IllegalArgumentException("Password must not be blank");

        String hashed = BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
        return new HashedPassword(hashed);
    }

    public boolean matches(String rawPassword) {
        return rawPassword != null && BCrypt.checkpw(rawPassword, value);
    }

    @Override
    public String toString(){
        return "Protected";
    }

}
