package org.resourceserver.modules.user.repository;

import org.resourceserver.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,String> {
    boolean existsByEmail(String email);
    java.util.Optional<User> findByEmail(String email);
}
