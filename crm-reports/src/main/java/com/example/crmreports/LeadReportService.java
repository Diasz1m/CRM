package com.example.crmreports;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
class LeadReportService {

    private static final ZoneId TIME_ZONE = ZoneId.of("America/Sao_Paulo");

    private final RestClient crmClient;

    LeadReportService(RestClient crmClient) {
        this.crmClient = crmClient;
    }

    LeadReportResponse report(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A data inicial deve ser anterior ou igual à data final.");
        }
        if (from.getYear() < 1 || to.getYear() > 9999) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe datas com anos entre 0001 e 9999.");
        }

        var start = from.atStartOfDay(TIME_ZONE).toInstant();
        var end = to.plusDays(1).atStartOfDay(TIME_ZONE).toInstant();
        List<LeadStageCount> rows;
        try {
            rows = crmClient.get()
                    .uri(builder -> builder.path("/internal/leads/stage-counts")
                            .queryParam("start", start).queryParam("end", end).build())
                    .retrieve().body(new ParameterizedTypeReference<List<LeadStageCount>>() {});
        } catch (RestClientException exception) {
            throw unavailable();
        }
        if (rows == null) {
            throw unavailable();
        }

        var counts = new EnumMap<LeadStage, Long>(LeadStage.class);
        for (LeadStage stage : LeadStage.values()) {
            counts.put(stage, 0L);
        }

        long total = 0;
        for (LeadStageCount row : rows) {
            counts.put(row.stage(), row.count());
            total += row.count();
        }
        return new LeadReportResponse(from, to, TIME_ZONE.getId(), total, counts);
    }

    private ResponseStatusException unavailable() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Não foi possível consultar os dados do CRM. Tente novamente em instantes.");
    }

    record LeadStageCount(LeadStage stage, long count) {}
}
