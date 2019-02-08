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
public class LowStockEvent {
    private final String shopId;
    private final String productId;
    private final String productName;

    @Override
    public String toString() {
        return "Product '" + productName + "' has low stock.";
    }
}
