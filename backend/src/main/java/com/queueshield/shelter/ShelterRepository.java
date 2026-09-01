package com.queueshield.shelter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ShelterRepository extends JpaRepository<Shelter, Long> {

    Page<Shelter> findByStatus(ShelterStatus status, Pageable pageable);

    @Query("select coalesce(sum(s.capacityTotal), 0) from Shelter s")
    long sumCapacityTotal();

    @Query("select coalesce(sum(s.capacityOccupied), 0) from Shelter s")
    long sumCapacityOccupied();
}
