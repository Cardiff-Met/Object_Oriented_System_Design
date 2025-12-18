package server;

public final class Researcher extends Employee {

    public Researcher(String userId) {
        super(userId);
    }

    @Override
    public EmployeeRole role() {
        return EmployeeRole.RESEARCHER;
    }
}
