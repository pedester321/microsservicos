package com.microsservicos.productapi.modules.supplier.model;


import com.microsservicos.productapi.modules.supplier.dto.SupplierRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "SUPPLIER")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(name = "NAME",nullable = false)
    private String name;

    public static Supplier of(SupplierRequest request){
        var supplier = new Supplier();
        BeanUtils.copyProperties(request,supplier);
        return supplier;
    }
}
