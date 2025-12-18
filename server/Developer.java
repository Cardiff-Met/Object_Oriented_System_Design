package server;

public final class Developer extends Employee {

    public Developer(String userId) {
        super(userId);
    }

    @Override
    public EmployeeRole role() {
        return EmployeeRole.DEVELOPER;
    }
}
