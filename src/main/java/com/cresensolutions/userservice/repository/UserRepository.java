package com.cresensolutions.userservice.repository;
import com.cresensolutions.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUserName(String userName);
    boolean existsByUserName(String userName);
    void deleteByUserName(String userName);
}
