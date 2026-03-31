package com.example.backend.repository;

import com.example.backend.entities.Customer;
import com.example.backend.repository.abstraction.IBaseEntityRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.stereotype.Repository;

@Repository
public interface ICustomerRepository extends IBaseEntityRepository<Customer,Long> {
    boolean existsByPhoneNumber(String phoneNumber);
}
