package server;

import java.time.LocalDateTime;

public interface Clock {
    LocalDateTime now();
}
