package be.kdg.poc.webshop.event;

import be.kdg.poc.product.dom.Product;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Cédric Goffin
 * 02/02/2019 14:31
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class ProductBoughtEvent {
    private final String shopId;
    private final String productId;

    @Override
    public String toString() {
        return "Product with id '" + productId + "' bought from shop with id '" + shopId + "'";
    }
}
