package com.vasyerp.Repository;

import com.vasyerp.Entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContactRepository extends JpaRepository<Contact, String> {

    @Query("""
        SELECT c FROM Contact c
        WHERE (:search IS NULL OR :search = ''
               OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<Contact> search(@Param("search") String search, Pageable pageable);
}