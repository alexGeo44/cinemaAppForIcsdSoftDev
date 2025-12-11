// src/main/java/com/cinema/infrastructure/config/DataInitializer.java
package com.cinema.infrastructure.config;

import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.HashedPassword;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.entity.value.Username;
import com.cinema.domain.enums.BaseRole;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.enums.ScreeningState;
import com.cinema.domain.policy.PasswordPolicy;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.ScreeningRepository;
import com.cinema.domain.port.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Configuration
public class DataInitializer {

    // =========================
    // MAIN SEED RUNNER
    // =========================
    @Bean
    public CommandLineRunner seedData(UserRepository userRepository,
                                      PasswordPolicy passwordPolicy,
                                      ProgramRepository programRepository,
                                      ScreeningRepository screeningRepository) {

        return args -> {
            System.out.println(">>> DataInitializer: START");

            seedUsers(userRepository, passwordPolicy);
            seedProgramsAndScreenings(userRepository, programRepository, screeningRepository);

            System.out.println(">>> DataInitializer: DONE");
        };
    }

    // =========================
    // USERS
    // =========================
    private void seedUsers(UserRepository userRepository,
                           PasswordPolicy passwordPolicy) {

        System.out.println(">>> DataInitializer: seeding users...");

        seedUserIfMissing(
                userRepository,
                passwordPolicy,
                "admin01",
                "Default Admin",
                "F3$CinemaHub",    // δυνατό password, χωρίς "admin"
                BaseRole.ADMIN
        );

        seedUserIfMissing(
                userRepository,
                passwordPolicy,
                "prog01",
                "Programmer One",
                "R8#FestivalX",
                BaseRole.PROGRAMMER
        );

        seedUserIfMissing(
                userRepository,
                passwordPolicy,
                "staff01",
                "Staff Member One",
                "T6!CrewShift",
                BaseRole.STAFF
        );

        seedUserIfMissing(
                userRepository,
                passwordPolicy,
                "submit01",
                "Submitter One",
                "Q5@TicketBox",
                BaseRole.SUBMITTER
        );
    }

    private void seedUserIfMissing(UserRepository userRepository,
                                   PasswordPolicy passwordPolicy,
                                   String rawUsername,
                                   String fullName,
                                   String rawPassword,
                                   BaseRole role) {

        boolean exists = userRepository.findAll().stream()
                .anyMatch(u -> u.username() != null
                        && u.username().value().equals(rawUsername));

        if (exists) {
            System.out.println(">>> User '" + rawUsername + "' already exists, skipping.");
            return;
        }

        try {
            Username username = new Username(rawUsername);

            // εφαρμόζουμε την κανονική policy σου
            passwordPolicy.validate(rawPassword, username, fullName).ensureValid();

            HashedPassword password = HashedPassword.fromRaw(rawPassword);

            long randomId = Math.abs(UUID.randomUUID().getLeastSignificantBits());

            User user = new User(
                    new UserId(randomId),
                    username,
                    password,
                    fullName,
                    role,
                    true,   // active
                    0       // version
            );

            userRepository.Save(user);
            System.out.println(">>> Created " + role + " user '" + rawUsername + "'");

        } catch (Exception e) {
            System.out.println(">>> Could not create user '" + rawUsername + "': " + e.getMessage());
        }
    }

    // =========================
    // PROGRAMS & SCREENINGS
    // =========================
    private void seedProgramsAndScreenings(UserRepository userRepository,
                                           ProgramRepository programRepository,
                                           ScreeningRepository screeningRepository) {

        System.out.println(">>> DataInitializer: seeding programs & screenings...");

        // Βρίσκουμε τους χρήστες που δημιουργήσαμε πιο πάνω
        Optional<User> adminOpt = findByUsername(userRepository, "admin01");
        Optional<User> progOpt  = findByUsername(userRepository, "prog01");
        Optional<User> staffOpt = findByUsername(userRepository, "staff01");
        Optional<User> submitOpt = findByUsername(userRepository, "submit01");

        if (adminOpt.isEmpty() || progOpt.isEmpty() || staffOpt.isEmpty() || submitOpt.isEmpty()) {
            System.out.println(">>> Some seed users are missing, skipping programs/screenings seeding.");
            return;
        }

        User admin = adminOpt.get();
        User programmer = progOpt.get();
        User staff = staffOpt.get();
        User submitter = submitOpt.get();

        // ---- Program 1: Main Festival 2025 ----
        Program festival2025 = new Program(
                null,   // ProgramId -> null, θα το αναλάβει η DB (IDENTITY)
                "Main Festival 2025",
                "Κύριο πρόγραμμα φεστιβάλ 2025.",
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 10),
                admin.id(),           // creatorUserId
                ProgramState.DRAFT    // ή ό,τι enum έχεις (DRAFT / PUBLISHED κλπ)
        );

        // Προσθέτουμε programmer & staff στο πρόγραμμα
        festival2025.addProgrammer(programmer.id());
        festival2025.addStaff(staff.id());

        festival2025 = programRepository.save(festival2025);
        System.out.println(">>> Created program: " + festival2025.name()
                + " (id will be generated)");

        // ---- Program 2: Autumn Specials 2025 ----
        Program autumn2025 = new Program(
                null,
                "Autumn Specials 2025",
                "Ειδικές προβολές φθινοπώρου 2025.",
                LocalDate.of(2025, 10, 1),
                LocalDate.of(2025, 10, 5),
                admin.id(),
                ProgramState.DRAFT
        );

        autumn2025.addProgrammer(programmer.id());
        autumn2025.addStaff(staff.id());

        autumn2025 = programRepository.save(autumn2025);
        System.out.println(">>> Created program: " + autumn2025.name());

        // =========================
        // SCREENINGS για τα παράδειγμα
        // =========================

        // 1) Screening σε CREATED state
        Screening draftScreening = new Screening(
                null,                      // ScreeningId -> null, DB identity
                festival2025.id(),
                submitter.id(),
                "Shorts Block A",
                "Shorts",
                "Συλλογή από μικρού μήκους ταινίες.",
                ScreeningState.CREATED
        );

        screeningRepository.save(draftScreening);
        System.out.println(">>> Created screening (CREATED): " + draftScreening.title());

        // 2) Screening που έχει γίνει SUBMITTED -> UNDER_REVIEW -> ACCEPTED -> SCHEDULED
        Screening acceptedScreening = new Screening(
                null,
                festival2025.id(),
                submitter.id(),
                "Midnight Horror",
                "Horror",
                "Late night horror screening.",
                ScreeningState.CREATED
        );

        // submit από submitter
        acceptedScreening.submit();
        // assign σε staff
        acceptedScreening.assignStaff(staff.id());
        // accept από staff
        acceptedScreening.accept();
        // schedule από programmer / staff
        acceptedScreening.schedule(
                LocalDate.of(2025, 5, 3),
                "Room 1"
        );

        screeningRepository.save(acceptedScreening);
        System.out.println(">>> Created screening (SCHEDULED): " + acceptedScreening.title());

        // 3) Screening σε REJECTED state
        Screening rejectedScreening = new Screening(
                null,
                autumn2025.id(),
                submitter.id(),
                "Experimental Vision",
                "Experimental",
                "Πειραματικές ταινίες.",
                ScreeningState.CREATED
        );

        rejectedScreening.submit();
        rejectedScreening.assignStaff(staff.id());
        rejectedScreening.reject();

        screeningRepository.save(rejectedScreening);
        System.out.println(">>> Created screening (REJECTED): " + rejectedScreening.title());
    }

    // helper για εύρεση user by username
    private Optional<User> findByUsername(UserRepository userRepository, String username) {
        try {
            return userRepository.findByUserName(new Username(username));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
