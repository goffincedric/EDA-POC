package be.kdg.poc.webshop.command;

import be.kdg.poc.product.dom.Product;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

/**
 * @author Cédric Goffin
 * 02/02/2019 13:25
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class AddProductCommand {
    @TargetAggregateIdentifier
    private final String shopId;
    private final Product product;
}
