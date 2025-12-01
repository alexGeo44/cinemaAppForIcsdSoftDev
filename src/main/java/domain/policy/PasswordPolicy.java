package domain.policy;

import domain.Exceptions.ValidationException;
import domain.entity.User;
import domain.entity.value.Username;

import java.util.*;
import java.util.regex.Pattern;

public final class PasswordPolicy {

    public record Config(
            int minLength,
            boolean requireUpper,
            boolean requireLower,
            boolean requireDigit,
            boolean requireSpecial,
            int maxRepeatSequence,
            int maxAscendingSequence,
            Set<String> bannedPasswords

    ){

        public static Config strongDefaults(){

            return new Config(
                    10,
                    true,
                    true,
                    true,
                    true,
                    3,
                    4,
                    new HashSet<>(List.of(
                            "password",
                            "123456",
                            "qwerty"
                    )));
        }


    }

    public record Violation(String code, String message) {}

    public record Result(boolean valid , List<Violation> violations){
        public void ensureValid(){
            if(!valid){
                throw new ValidationException(
                        "PasswordPolicy", String.join(";",
                        violations.stream().map(Violation::message).toList()));
            }
        }
    }

    private final Config cfg;

    public PasswordPolicy(Config cfg){ this.cfg = Objects.requireNonNull(cfg); }

    public Result validate(String rawPassword, Username username, String fullName) {
        List<Violation> v = new ArrayList<>();
        if (rawPassword == null || rawPassword.isBlank()) {
            v.add(new Violation("blank", "Password cannot be blank"));
            return new Result(false, v);
        }

    String p =rawPassword;
        if (p.length() < cfg.minLength()) v.add(new Violation("length", "Minimum length: " + cfg.minLength()));

        if (cfg.requireUpper() && !Pattern.compile("[A-Z]").matcher(p).find())
            v.add(new Violation("upper", "At least one uppercase letter required"));
        if (cfg.requireLower() && !Pattern.compile("[a-z]").matcher(p).find())
            v.add(new Violation("lower", "At least one lowercase letter required"));
        if (cfg.requireDigit() && !Pattern.compile("\\d").matcher(p).find())
            v.add(new Violation("digit", "At least one digit required"));
        if (cfg.requireSpecial() && !Pattern.compile("[^A-Za-z0-9]").matcher(p).find())
            v.add(new Violation("special", "At least one special character required"));

        // simple repeats: e.g., more than N same chars in a row
        if (cfg.maxRepeatSequence() > 0 && hasRepeatSequence(p, cfg.maxRepeatSequence() + 1))
            v.add(new Violation("repeat", "Too many repeated characters in a row"));

        // simple ascending sequences like 1234, abcd
        if (cfg.maxAscendingSequence() > 0 && hasAscendingSequence(p, cfg.maxAscendingSequence() + 1))
            v.add(new Violation("sequence", "Contains long ascending sequence"));

        // banned/common
        if (cfg.bannedPasswords() != null && cfg.bannedPasswords().contains(p.toLowerCase()))
            v.add(new Violation("banned", "Password is too common"));

        // avoid using username / full name fragments
        if (username != null && containsIgnoreCase(p, username.value()))
            v.add(new Violation("username-fragment", "Password must not contain the username"));
        if (fullName != null && anyTokenContained(p, fullName))
            v.add(new Violation("name-fragment", "Password must not contain parts of your name"));

        return new Result(v.isEmpty(), Collections.unmodifiableList(v));
    }

    public boolean isStrong(String rawPassword) {
        return validate(rawPassword, null, null).valid();
    }

    private static boolean hasRepeatSequence(String s, int threshold) {
        int run = 1;
        for (int i = 1; i < s.length(); i++) {
            run = (s.charAt(i) == s.charAt(i - 1)) ? run + 1 : 1;
            if (run >= threshold) return true;
        }
        return false;
    }

    private static boolean hasAscendingSequence(String s, int threshold) {
        int run = 1;
        for (int i = 1; i < s.length(); i++) {
            int prev = s.charAt(i - 1), cur = s.charAt(i);
            run = (cur == prev + 1) ? run + 1 : 1;
            if (run >= threshold) return true;
        }
        return false;
    }

    private static boolean containsIgnoreCase(String haystack, String needle) {
        return haystack.toLowerCase().contains(needle.toLowerCase());
    }

    private static boolean anyTokenContained(String password, String fullName) {
        String[] parts = fullName.toLowerCase().split("\\s+");
        String p = password.toLowerCase();
        for (String token : parts) {
            if (token.length() >= 3 && p.contains(token)) return true;
        }
        return false;
    }



}
