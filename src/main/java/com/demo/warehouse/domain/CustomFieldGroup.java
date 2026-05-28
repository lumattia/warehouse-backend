package com.demo.warehouse.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "custom_field_groups")
public class CustomFieldGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer groupOrder;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ModuleType module;

    @Column(nullable = false)
    private UUID tenantId;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @jakarta.persistence.OrderBy("fieldOrder ASC")
    private List<CustomFieldDefinition> definitions = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (groupOrder == null) {
            groupOrder = 0;
        }
    }
}
