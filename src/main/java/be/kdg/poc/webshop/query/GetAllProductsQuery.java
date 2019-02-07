package be.kdg.poc.webshop.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

/**
 * @author Cédric Goffin
 * 07/02/2019 11:04
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class GetAllProductsQuery {
    private final String shopId;
}
