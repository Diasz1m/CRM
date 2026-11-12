package com.example.crm.leads;

import java.time.Instant;
import java.time.LocalDate;

record LeadResponse(
        Long id,
        String name,
        String email,
        String phone,
        String source,
        LeadStage stage,
        String nextAction,
        LocalDate nextActionDate,
        Instant createdAt
) {}
