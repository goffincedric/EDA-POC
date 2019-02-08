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
public class ProductAddedEvent {
    private final String shopId;
    private final Product product;

    @Override
    public String toString() {
        return "Added product '" + product.getName() + "'";
    }
}
