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
public class WebshopDeletedEvent {
    private final String id;

    @Override
    public String toString() {
        return "Deleted shop with id '" + id + "'";
    }
}
