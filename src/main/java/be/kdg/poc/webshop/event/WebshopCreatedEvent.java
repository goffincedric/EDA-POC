package be.kdg.poc.webshop.event;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Cédric Goffin
 * 02/02/2019 14:14
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class WebshopCreatedEvent {
    private final String id;
    private final String name;
    private final double balance;

    @Override
    public String toString() {
        return "Created shop '" + name + "' with id '" + id + "'";
    }
}
