package com.demo.warehouse.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @ElementCollection(targetClass = ModuleType.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "tenant_modules", joinColumns = @JoinColumn(name = "tenant_id"))
    @Column(name = "module_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<ModuleType> modules = new HashSet<>();

    @Column(nullable = false)
    private Boolean hasCustomFields = true;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        expiresAt = createdAt.plusSeconds(24 * 60 * 60);
    }
}
