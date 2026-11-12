package com.example.crm.products;

import java.math.BigDecimal;
import java.time.Instant;

record ProductResponse(Long id, String name, String description, BigDecimal price, Instant createdAt) {}
