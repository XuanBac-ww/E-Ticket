package com.example.backend.repository;

import com.example.backend.entities.Event;
import com.example.backend.repository.abstraction.IBaseEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IEventRepository extends IBaseEntityRepository<Event,Long> {
}
