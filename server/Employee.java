package server;

import java.util.Objects;

public abstract class Employee {

    private final String userId;

    protected Employee(String userId) {
        this.userId = Objects.requireNonNull(userId, "userId cannot be null").trim();
        if (this.userId.isEmpty()) {
            throw new IllegalArgumentException("userId cannot be empty");
        }
    }

    public final String userId() {
        return userId;
    }

    public abstract EmployeeRole role();
}
