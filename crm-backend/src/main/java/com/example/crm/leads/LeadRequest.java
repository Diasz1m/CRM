package com.example.crm.leads;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

record LeadRequest(
        @NotBlank @Size(max = 150) String name,
        @Email @Size(max = 254) String email,
        @Size(max = 40) String phone,
        @Size(max = 100) String source,
        LeadStage stage,
        @Size(max = 255) String nextAction,
        LocalDate nextActionDate
) {
    LeadRequest {
        name = normalize(name);
        email = normalize(email);
        phone = normalize(phone);
        source = normalize(source);
        nextAction = normalize(nextAction);
    }

    @AssertTrue(message = "Informe o e-mail ou o telefone.")
    public boolean isContactPresent() {
        return email != null || phone != null;
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
