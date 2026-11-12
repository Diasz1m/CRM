package com.example.crm.leads;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface LeadRepository extends JpaRepository<Lead, Long> {

    @Query("""
            select new com.example.crm.leads.LeadStageCount(lead.stage, count(lead))
            from Lead lead
            where lead.createdAt >= :start and lead.createdAt < :end
            group by lead.stage
            """)
    List<LeadStageCount> countByStageInPeriod(Instant start, Instant end);

    Page<Lead> findByStage(LeadStage stage, Pageable page);

    Page<Lead> findByNameContainingIgnoreCase(String name, Pageable page);

    Page<Lead> findByStageAndNameContainingIgnoreCase(LeadStage stage, String name, Pageable page);
}
