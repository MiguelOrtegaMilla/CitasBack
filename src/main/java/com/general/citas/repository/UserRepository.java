package com.general.citas.repository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.general.citas.model.User;



@Repository
public interface UserRepository extends JpaRepository<User , Long> {

    Optional<User> findByName(String name);

    Optional<User> findByEmail(String email);

    Optional<User> findByUuid(String uuid);

    boolean existsByName(String name);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}
