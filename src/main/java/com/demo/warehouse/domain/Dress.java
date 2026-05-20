package com.demo.warehouse.domain;

import java.math.BigDecimal;

import com.demo.warehouse.mapper.IdName;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "dresses",uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sku", "tenant_id"}) // The combination must be unique
})
public class Dress extends TenantScopedEntity implements IdName<Long>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 64)
    private String sku;

    @Column(length = 64)
    private String size;

    @Column(length = 64)
    private String color;
    @Column(nullable = false)
    private Integer stock = 0;
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Override
    public String getName(){
        return this.sku;
    }
    public void addStock(Integer quantity){
        if (quantity == null || quantity == 0) {
            throw new IllegalArgumentException("The quantity to modify must be different from zero.");
        }
    
        // 2. Calculate the resulting stock
        int result = this.stock + quantity;
    
        // 3. Validate that the result is not negative
        if (result < 0) {
            throw new IllegalArgumentException("Operation not allowed: Final stock cannot be negative (Current stock: " + this.stock + ")");
        }
    
        this.stock = result;
    }
}
