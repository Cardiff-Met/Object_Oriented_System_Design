package server;

public final class EmployeeFactory {

    private EmployeeFactory() {}

    public static Employee fromUserId(String userId) {
        if (userId == null) {
            return new Researcher("unknown");
        }

        String trimmed = userId.trim();
        String lower = trimmed.toLowerCase();

        if (lower.startsWith("admin:") || lower.startsWith("admin-") || lower.startsWith("a-") || lower.startsWith("a:")) {
            return new Admin(trimmed);
        }
        if (lower.startsWith("dev:") || lower.startsWith("dev-") || lower.startsWith("d-") || lower.startsWith("d:")) {
            return new Developer(trimmed);
        }
        if (lower.startsWith("researcher:") || lower.startsWith("researcher-") || lower.startsWith("r-") || lower.startsWith("r:")) {
            return new Researcher(trimmed);
        }

        return new Researcher(trimmed);
    }
}
