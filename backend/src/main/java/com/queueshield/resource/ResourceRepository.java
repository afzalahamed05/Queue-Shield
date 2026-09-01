package com.queueshield.resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    Page<Resource> findByType(ResourceType type, Pageable pageable);

    Page<Resource> findByStatus(ResourceStatus status, Pageable pageable);

    Page<Resource> findByTypeAndStatus(ResourceType type, ResourceStatus status, Pageable pageable);

    @Query("select coalesce(sum(r.quantityAvailable), 0) from Resource r")
    long sumQuantityAvailable();

    @Query("select coalesce(sum(r.quantityTotal), 0) from Resource r")
    long sumQuantityTotal();
}
