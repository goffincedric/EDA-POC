package be.kdg.poc.configuration;

import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.tokenstore.inmemory.InMemoryTokenStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * @author Cédric Goffin
 * 05/02/2019 17:02
 */
@Configuration
public class AggregateConfig {

    @Autowired
    public void configureInMemoryTokenStore(EventProcessingConfigurer configurer) {
        configurer.registerTokenStore(configuration -> new InMemoryTokenStore());
    }
}
