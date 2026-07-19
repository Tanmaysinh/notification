package com.vasyerp.Repository;

import com.vasyerp.Entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RequestStatusRepository extends JpaRepository<RequestStatus, String> {
    Optional<RequestStatus> findByRequestRequestIdAndContactId(String requestId, String contactId);
}