package be.kdg.poc.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Cédric Goffin
 * 02/02/2019 14:34
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private String id;

    private String name;

    // Retail price
    private double retailPrice;

    // Price of product at wholesale
    private double buyPrice;
}
