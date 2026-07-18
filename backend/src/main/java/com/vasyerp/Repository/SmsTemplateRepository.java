package com.vasyerp.Repository;

import com.vasyerp.Entity.SmsTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SmsTemplateRepository extends JpaRepository<SmsTemplate, String> {

    @Query("""
        SELECT t FROM SmsTemplate t
        WHERE (:search IS NULL OR :search = ''
               OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<SmsTemplate> search(@Param("search") String search, Pageable pageable);
}