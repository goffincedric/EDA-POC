package be.kdg.poc.product.dom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author Cédric Goffin
 * 02/02/2019 13:08
 */

@Data
@NoArgsConstructor
public class Product {
    private String id;

    private String name;

    // Retail price
    private double retailPrice;

    // Discount percentage on retail price
    private double discountPercentage;

    // Price of product at wholesale
    private double buyPrice;

    public Product(String name, double retailPrice, double buyPrice) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.retailPrice = retailPrice;
        this.buyPrice = buyPrice;
    }
}
