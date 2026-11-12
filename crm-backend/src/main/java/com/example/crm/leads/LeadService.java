package com.example.crm.leads;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
class LeadService {

    private final LeadRepository leads;

    LeadService(LeadRepository leads) {
        this.leads = leads;
    }

    Page<LeadResponse> list(LeadStage stage, String query, Pageable page) {
        String name = query == null ? "" : query.strip();
        if (stage == null && name.isEmpty()) {
            return leads.findAll(page).map(Lead::toResponse);
        }
        if (stage == null) {
            return leads.findByNameContainingIgnoreCase(name, page).map(Lead::toResponse);
        }
        if (name.isEmpty()) {
            return leads.findByStage(stage, page).map(Lead::toResponse);
        }
        return leads.findByStageAndNameContainingIgnoreCase(stage, name, page).map(Lead::toResponse);
    }

    LeadResponse get(long id) {
        return find(id).toResponse();
    }

    @Transactional
    LeadResponse create(LeadRequest request) {
        return leads.save(new Lead(request)).toResponse();
    }

    @Transactional
    LeadResponse update(long id, LeadRequest request) {
        Lead lead = find(id);
        lead.update(request);
        return lead.toResponse();
    }

    private Lead find(long id) {
        return leads.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead não encontrado."));
    }
}
