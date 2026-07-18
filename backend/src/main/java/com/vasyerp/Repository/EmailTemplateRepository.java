package com.vasyerp.Repository;

import com.vasyerp.Entity.EmailTemplate;
import com.vasyerp.Entity.SmsTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, String> {

    @Query("""
        SELECT t FROM EmailTemplate t
        WHERE (:search IS NULL OR :search = ''
               OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<EmailTemplate> search(@Param("search") String search, Pageable pageable);
}