package be.kdg.poc.webshop.query;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Cédric Goffin
 * 02/02/2019 14:04
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class GetCurrentDiscountedPriceQuery {
    private final String shopId;
    private final String productId;
}
