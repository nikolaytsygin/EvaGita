package com.eva.evagita.repository;

import com.eva.evagita.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from User user where user.username = :username")
    Optional<User> findByUsernameForUpdate(@Param("username") String username);

    @Query("select user from User user where lower(user.email) = lower(:email)")
    Optional<User> findByEmail(@Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from User user where lower(user.email) = lower(:email)")
    Optional<User> findByEmailForUpdate(@Param("email") String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
