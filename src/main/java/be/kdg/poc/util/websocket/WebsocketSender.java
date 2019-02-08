package be.kdg.poc.util.websocket;

import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Cédric Goffin
 * 08/02/2019 15:27
 */
@Component
public class WebsocketSender {
    /*private final EventBus eventBus;
    private final EventProcessingConfigurer eventProcessingConfigurer;
    private final MessageDispatchInterceptor interceptor;*/

    private final SimpMessagingTemplate simpleMessagingTemplate;

    @Autowired
    public WebsocketSender(SimpMessagingTemplate simpleMessagingTemplate) {
        this.simpleMessagingTemplate = simpleMessagingTemplate;
    }

    public void sendMessage(String destination, Object content) {
        simpleMessagingTemplate.convertAndSend(destination, content);
    }
}
