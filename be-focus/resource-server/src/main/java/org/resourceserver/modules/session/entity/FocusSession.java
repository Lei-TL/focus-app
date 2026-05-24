package org.resourceserver.modules.session.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "focus_sessions")
public class FocusSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private Long roomId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Column(nullable = false)
    private String startedBy;

    private Instant startTime;
    private Instant endTime;
    private Instant actualEndTime;

    @Column(nullable = false)
    private int durationSeconds;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    public enum SessionStatus {
        WAITING,
        FOCUSING,
        COMPLETED,
        CANCELLED
    }
}
