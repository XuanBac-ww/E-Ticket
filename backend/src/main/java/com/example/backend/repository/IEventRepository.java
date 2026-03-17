package com.example.backend.repository;

import com.example.backend.entities.Event;
import com.example.backend.repository.abstraction.IBaseEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IEventRepository extends IBaseEntityRepository<Event,Long> {

    @Query("""
       SELECT e
           from Event e
             WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(e.location) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<Event> searchEvents(String keyword, Pageable pageable);

    boolean existsEventByTitle(String title);
}
