package com.example.crm.leads;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestController
@RequestMapping("/api/leads")
class LeadController {

    private final LeadService leads;

    LeadController(LeadService leads) {
        this.leads = leads;
    }

    @GetMapping
    PagedModel<LeadResponse> list(
            @RequestParam(required = false) LeadStage stage,
            @RequestParam(required = false) String q,
            Pageable pageable) {
        var page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id").descending());
        return new PagedModel<>(leads.list(stage, q, page));
    }

    @GetMapping("/{id}")
    LeadResponse get(@PathVariable long id) {
        return leads.get(id);
    }

    @PostMapping
    ResponseEntity<LeadResponse> create(@Valid @RequestBody LeadRequest request) {
        var lead = leads.create(request);
        return ResponseEntity.created(URI.create("/api/leads/" + lead.id())).body(lead);
    }

    @PutMapping("/{id}")
    LeadResponse update(@PathVariable long id, @Valid @RequestBody LeadRequest request) {
        return leads.update(id, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail invalidLead(MethodArgumentNotValidException exception) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Verifique os dados do lead.");
        var errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldError(error.getField(), error.getDefaultMessage()))
                .toList();
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail concurrentUpdate() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "O lead foi alterado durante a atualização. Consulte o lead e tente novamente.");
    }

    record FieldError(String field, String message) {}
}
