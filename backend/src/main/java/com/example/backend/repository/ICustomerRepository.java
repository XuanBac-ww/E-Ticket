package com.example.backend.repository;

import com.example.backend.entities.Customer;
import com.example.backend.repository.abstraction.IBaseEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICustomerRepository extends IBaseEntityRepository<Customer,Long> {
    boolean existsByPhoneNumber(String phoneNumber);
}
