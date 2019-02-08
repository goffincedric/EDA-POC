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
public class WebshopDeletedEvent {
    private final String id;
    private final String name;

    @Override
    public String toString() {
        return "Deleted shop '" + id + "'.";
    }
}
