package org.resourceserver.modules.session.repository;

import org.resourceserver.modules.session.entity.FocusSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FocusSessionRepository extends JpaRepository<FocusSession, String> {
    List<FocusSession> findByRoomId(Long roomId);
    Optional<FocusSession> findFirstByRoomIdAndStatusOrderByCreatedAtDesc(Long roomId, FocusSession.SessionStatus status);
    List<FocusSession> findByStatus(FocusSession.SessionStatus status);
}
