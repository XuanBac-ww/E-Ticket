package com.example.backend.repository;

import com.example.backend.entities.Admin;
import com.example.backend.repository.abstraction.IBaseEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAdminRepository extends IBaseEntityRepository<Admin, Long> {
}
