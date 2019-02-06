package be.kdg.poc.webshop.command;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import org.springframework.stereotype.Component;

/**
 * @author Cédric Goffin
 * 02/02/2019 13:23
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class CreateShopCommand {
    @TargetAggregateIdentifier
    private final String id;
    private final String name;
}
