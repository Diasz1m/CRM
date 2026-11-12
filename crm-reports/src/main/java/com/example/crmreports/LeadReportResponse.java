package com.example.crmreports;

import java.time.LocalDate;
import java.util.Map;

record LeadReportResponse(
        LocalDate from,
        LocalDate to,
        String timeZone,
        long total,
        Map<LeadStage, Long> byStage
) {}
