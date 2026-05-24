package org.resourceserver.modules.stats.entity;

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
@Table(name = "focus_records")
public class FocusRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    private Long roomId;
    private String sessionId;

    @Enumerated(EnumType.STRING)
    private Mode mode;

    @Column(nullable = false)
    private int durationSeconds;

    private double completionRate;

    @Column(nullable = false)
    private boolean completed;

    private Instant startedAt;
    private Instant endedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    public enum Mode {
        SOLO,
        ROOM
    }
}
