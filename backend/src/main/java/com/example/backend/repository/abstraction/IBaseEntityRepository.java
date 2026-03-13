package com.example.backend.repository.abstraction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface IBaseEntityRepository<T,ID> extends JpaRepository<T,ID> {
}
