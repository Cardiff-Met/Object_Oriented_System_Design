package server;

public final class Admin extends Employee {

    public Admin(String userId) {
        super(userId);
    }

    @Override
    public EmployeeRole role() {
        return EmployeeRole.ADMIN;
    }
}
