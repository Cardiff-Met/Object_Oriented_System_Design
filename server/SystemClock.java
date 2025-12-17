package server;

import java.time.LocalDateTime;

public final class SystemClock implements Clock {
    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
