package com.example.backend.repository;

import com.example.backend.entities.User;
import com.example.backend.repository.abstraction.IBaseEntityRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface IUserRepository extends IBaseEntityRepository<User,Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
