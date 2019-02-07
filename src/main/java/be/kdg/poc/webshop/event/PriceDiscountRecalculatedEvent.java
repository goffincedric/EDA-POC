package be.kdg.poc.webshop.event;

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
public class PriceDiscountRecalculatedEvent {
    private final String shopId;
    private final String productId;

    @Override
    public String toString() {
        return "Price discount of product with id '" + productId + "' from shop with id '" + shopId + "' has been recalculated";
    }
}
