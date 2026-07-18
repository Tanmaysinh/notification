package com.vasyerp.Repository;

import com.vasyerp.Entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestStatusRepository extends JpaRepository<RequestStatus, String> {
}