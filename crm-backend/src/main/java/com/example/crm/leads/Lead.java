package com.example.crm.leads;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "leads")
class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 254)
    private String email;

    @Column(length = 40)
    private String phone;

    @Column(length = 100)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LeadStage stage;

    @Column(length = 255)
    private String nextAction;

    private LocalDate nextActionDate;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    private long version;

    protected Lead() {}

    Lead(LeadRequest request) {
        createdAt = Instant.now();
        stage = LeadStage.NEW;
        update(request);
    }

    void update(LeadRequest request) {
        name = request.name();
        email = request.email();
        phone = request.phone();
        source = request.source();
        nextAction = request.nextAction();
        nextActionDate = request.nextActionDate();
        if (request.stage() != null) {
            stage = request.stage();
        }
    }

    LeadResponse toResponse() {
        return new LeadResponse(id, name, email, phone, source, stage, nextAction, nextActionDate, createdAt);
    }
}
