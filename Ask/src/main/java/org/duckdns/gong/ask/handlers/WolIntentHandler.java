package org.duckdns.gong.ask.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import org.duckdns.gong.ask.Client;
import java.util.Optional;
import static com.amazon.ask.request.Predicates.intentName;

public class WolIntentHandler implements RequestHandler {
    private Client cl;

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("WolIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        cl = new Client();
        String speechText="Ok. I will send wol request";

        cl.enterServer();
        cl.sendStr("req wol please");
        cl.closeConnect();

        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("WolIntent", speechText)
                .build();
    }
}
