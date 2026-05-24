package org.resourceserver.modules.stats.repository;

import org.resourceserver.modules.stats.entity.FocusRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FocusRecordRepository extends JpaRepository<FocusRecord, String> {
    List<FocusRecord> findByUserId(String userId);
}
