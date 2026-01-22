package com.ao.repository;

import com.ao.entity.Tender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenderRepository extends JpaRepository<Tender, Long> {
    boolean existsBySourceId(String sourceId);
}
