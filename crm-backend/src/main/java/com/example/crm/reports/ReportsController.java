package com.example.crm.reports;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/reports/leads")
class ReportsController {

    private final RestClient reportsClient;
    private final boolean configured;

    ReportsController(RestClient reportsClient, @Value("${crm.reports.password}") String password) {
        this.reportsClient = reportsClient;
        configured = !password.isEmpty();
    }

    @GetMapping
    ResponseEntity<String> report(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (!configured) {
            throw unavailable();
        }
        try {
            return reportsClient.get()
                    .uri(builder -> builder.path("/api/reports/leads")
                            .queryParam("from", from).queryParam("to", to).build())
                    .retrieve().toEntity(String.class);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 400) {
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                        .body(exception.getResponseBodyAsString());
            }
            throw unavailable();
        } catch (RestClientException exception) {
            throw unavailable();
        }
    }

    private ResponseStatusException unavailable() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "O serviço de relatórios está indisponível. Tente novamente em instantes.");
    }
}
