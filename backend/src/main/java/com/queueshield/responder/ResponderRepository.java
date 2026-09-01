package com.queueshield.responder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponderRepository extends JpaRepository<Responder, Long> {

    Page<Responder> findByRole(ResponderRole role, Pageable pageable);

    Page<Responder> findByStatus(ResponderStatus status, Pageable pageable);

    Page<Responder> findByRoleAndStatus(ResponderRole role, ResponderStatus status, Pageable pageable);

    long countByStatus(ResponderStatus status);
}
