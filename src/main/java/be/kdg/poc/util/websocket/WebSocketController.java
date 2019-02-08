package be.kdg.poc.util.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    //@MessageMapping("/event")
    //@SendTo("/topic/greetings")
    //public void greeting(HelloMessage message) throws Exception {
    //    Thread.sleep(1000); // simulated delay
    //    return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
    //}
}
