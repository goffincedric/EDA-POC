package be.kdg.poc.webshop.command;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

/**
 * @author Cédric Goffin
 * 02/02/2019 13:24
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class DeleteWebshopCommand {
    @TargetAggregateIdentifier
    private final String shopId;
}
