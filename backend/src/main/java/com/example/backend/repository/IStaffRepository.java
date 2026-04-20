package com.example.backend.repository;

import com.example.backend.entities.Staff;
import com.example.backend.repository.abstraction.IBaseEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IStaffRepository extends IBaseEntityRepository<Staff, Long> {
    boolean existsByStaffCode(String staffCode);
}
