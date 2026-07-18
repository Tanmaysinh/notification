package com.vasyerp.Repository;

import com.vasyerp.Entity.PushTemplate;
import com.vasyerp.Entity.SmsTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PushTemplateRepository extends JpaRepository<PushTemplate, String> {

    @Query("""
        SELECT t FROM PushTemplate t
        WHERE (:search IS NULL OR :search = ''
               OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<PushTemplate> search(@Param("search") String search, Pageable pageable);
}