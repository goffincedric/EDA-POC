package be.kdg.poc.webshop.command;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

/**
 * @author Cédric Goffin
 * 02/02/2019 13:53
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class BuyProductCommand {
    @TargetAggregateIdentifier
    private final String shopId;
    private final String productId;
}
