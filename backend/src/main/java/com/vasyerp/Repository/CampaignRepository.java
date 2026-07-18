package com.vasyerp.Repository;

import com.vasyerp.Entity.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CampaignRepository extends JpaRepository<Campaign, String> {

    @Query("""
        SELECT c FROM Campaign c
        WHERE (:search IS NULL OR :search = ''
               OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<Campaign> search(@Param("search") String search, Pageable pageable);
}