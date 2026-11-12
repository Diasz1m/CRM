package com.example.crm.leads;

import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/internal/leads/stage-counts")
class LeadStatisticsController {

    private final LeadRepository leads;

    LeadStatisticsController(LeadRepository leads) {
        this.leads = leads;
    }

    @GetMapping
    @Transactional(readOnly = true)
    List<LeadStageCount> counts(@RequestParam Instant start, @RequestParam Instant end) {
        if (!start.isBefore(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O início do intervalo deve ser anterior ao fim.");
        }
        return leads.countByStageInPeriod(start, end);
    }
}
