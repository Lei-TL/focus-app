package org.resourceserver.modules.session.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sessions")
public class Session {
    @Id
    private String id;

    private Long roomId;
    private int durationMinutes;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum SessionStatus {
        WAITING,
        FOCUSING,
        COMPLETED,
        ENDED
    }

    public long calculateRemainingSeconds() {
        if (status != SessionStatus.FOCUSING || endTime == null) {
            return 0;
        }
        long remaining = java.time.Duration.between(LocalDateTime.now(), endTime).getSeconds();
        return Math.max(0, remaining);
    }
}
