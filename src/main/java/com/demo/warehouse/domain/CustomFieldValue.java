package com.demo.warehouse.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "custom_field_values", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"definition_id", "target_id"}))
public class CustomFieldValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "definition_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // 👈 Esto añade 'ON DELETE CASCADE' a la clave foránea DDL
    private CustomFieldDefinition definition;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false, length = 1000)
    private String value;
}
