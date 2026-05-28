package com.demo.warehouse.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "custom_field_definitions")
public class CustomFieldDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private CustomFieldGroup group;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CustomFieldType type;

    @Column(nullable = false)
    private Integer fieldOrder;

    @JdbcTypeCode(SqlTypes.JSON)
    private FieldValidations validations;

    @PrePersist
    void prePersist() {
        if (fieldOrder == null) {
            fieldOrder = 0;
        }
        if (validations == null) {
            validations = new FieldValidations(null, null, null, null, null);
        }
    }
}
