package com.ordermanagement.platform.auth.repository;

import com.ordermanagement.platform.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCase(
            String email
    );

    Optional<User> findByEmailIgnoreCase(
            String email
    );
}
