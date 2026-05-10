package com.demo.warehouse.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "dresses")
public class Dress extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, unique = true, length = 64)
    private String sku;

    @Column(length = 64)
    private String size;

    @Column(length = 64)
    private String color;
    private Integer stock;
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    public String getName(){
        return this.sku;
    }
    public void addStock(Integer quantity){
        if (quantity == null || quantity == 0) {
            throw new IllegalArgumentException("La cantidad a modificar debe ser distinta de cero.");
        }
    
        // 2. Calcular el stock resultante
        int result = this.stock + quantity;
    
        // 3. Validar que el resultado no sea negativo
        if (result < 0) {
            throw new IllegalArgumentException("Operación no permitida: El stock final no puede ser negativo (Stock actual: " + this.stock + ")");
        }
    
        this.stock = result;
    }
}
