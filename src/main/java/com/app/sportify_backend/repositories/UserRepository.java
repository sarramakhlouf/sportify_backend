package com.app.sportify_backend.repositories;

import com.app.sportify_backend.models.Role;
import com.app.sportify_backend.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPlayerCode(String playerCode);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    List<User> findByRoleAndIsEnabled(Role role, boolean isEnabled);
    List<User> findByRoleAndIsEnabledTrue(Role role);
}
