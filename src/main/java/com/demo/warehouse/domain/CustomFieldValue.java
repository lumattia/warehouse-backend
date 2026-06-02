package com.demo.warehouse.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "definition_id", nullable = false)
    private CustomFieldDefinition definition;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false, length = 1000)
    private String value;
}
