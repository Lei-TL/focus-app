package org.resourceserver.modules.session.repository;

import org.resourceserver.modules.session.entity.SessionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionParticipantRepository extends JpaRepository<SessionParticipant, Long> {
    List<SessionParticipant> findBySessionId(String sessionId);
    Optional<SessionParticipant> findBySessionIdAndUserId(String sessionId, String userId);
    long countBySessionId(String sessionId);
}
