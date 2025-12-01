package domain.entity;

import domain.entity.value.HashedPassword;
import domain.entity.value.UserId;
import domain.entity.value.Username;
import domain.enums.BaseRole;

public class User {
    private final UserId id;
    private Username username;
    private HashedPassword password;
    private String fullName;
    private BaseRole baseRole;
    private boolean active;
    private int failedAttempts;



    public User(
            UserId id,
            Username username,
            HashedPassword password,
            String fullName,
            BaseRole baseRole,
            boolean active,
            int failedAttempts
    ) {

        if(username == null)
            throw new IllegalArgumentException("Username cannot be null");
        if (password == null)
            throw new IllegalArgumentException("Password cannot be null");
        if (baseRole == null)
            throw new IllegalArgumentException("Base role cannot be null");

        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.baseRole = baseRole;
        this.active = active;
        this.failedAttempts = failedAttempts;
    }

    public void updateFullName(String newFullName){
        if (newFullName == null || newFullName.isBlank()) throw new IllegalArgumentException("Full name cannot be blank");
        this.fullName = newFullName.trim();
    }

    public void changePassword(HashedPassword newPassword){
        if(newPassword == null) throw new IllegalArgumentException("Password cannot be null");
        this.password = newPassword;
        this.failedAttempts = 0;
    }

    public void changeRole(BaseRole newRole){
        if(newRole == null) throw new IllegalArgumentException("Role cannot be null");
        this.baseRole = newRole;
    }

    public void activate(){ this.active = true; }

    public void deactivate(){ this.active = false; }

    public void registerFailedLogin(){ this.failedAttempts++; }
    public void resetFailedAttempts(){ this.failedAttempts = 0; }

    public boolean isLocked() { return failedAttempts >= 3; }

    public UserId id(){ return id; }
    public Username username(){ return username; }
    public HashedPassword password(){ return password; }
    public String fullName(){ return fullName; }
    public boolean isActive() {return active; }
    public int failedAttempts(){ return failedAttempts; }

}
