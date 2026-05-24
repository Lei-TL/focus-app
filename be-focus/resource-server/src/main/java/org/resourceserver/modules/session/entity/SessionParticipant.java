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
@Table(name = "session_participants")
public class SessionParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant joinedAt;

    private Instant leftAt;
    private Instant completedAt;

    private int focusDurationSeconds;

    private double completionRate;

    @Column(nullable = false)
    private boolean completed;

    public enum ParticipantStatus {
        FOCUSING,
        TEMP_OFFLINE,
        COMPLETED,
        LEFT_EARLY,
        DISCONNECTED
    }
}
