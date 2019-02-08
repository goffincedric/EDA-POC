package be.kdg.poc.util.websocket;

import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Cédric Goffin
 * 08/02/2019 15:27
 */
@Component
public class WebsocketSender {
    private final SimpMessagingTemplate simpleMessagingTemplate;

    private final SimpleDateFormat eventMessageDateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    public WebsocketSender(EventBus eventBus, SimpMessagingTemplate simpleMessagingTemplate) {
        this.simpleMessagingTemplate = simpleMessagingTemplate;

        // Submit any new events to websocket
        eventBus.subscribe(eventMessages -> eventMessages.stream()
                .map(this::convertEventToString)
                .forEach(message -> sendMessage("/app/event", message)));
    }

    public String convertEventToString(EventMessage message) {
        return eventMessageDateFormat.format(Date.from(message.getTimestamp())) +
                " - " +
                message.getPayload().toString();
    }

    public void sendMessage(String destination, Object content) {
        simpleMessagingTemplate.convertAndSend(destination, content);
    }
}
