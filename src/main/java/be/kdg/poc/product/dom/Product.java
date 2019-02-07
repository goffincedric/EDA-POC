package be.kdg.poc.product.dom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * @author Cédric Goffin
 * 02/02/2019 13:08
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    public double getDiscountedRetailPrice() {
        return retailPrice * (1 - discountPercentage);
    }

    @Override
    public String toString() {
        return "Id: '" + id + "', Name: '" + name + "', RetailPrice: " + retailPrice + "', BuyPrice: " + buyPrice + "', DiscountPercentage: " + discountPercentage;
    }
}
