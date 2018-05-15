package org.duckdns.gong.ask.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import org.duckdns.gong.ask.Client;
import java.util.Optional;
import static com.amazon.ask.request.Predicates.intentName;

public class NotiIntentHandler implements RequestHandler {
    private Client cl;

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("NotiIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        cl = new Client();
        cl.enterServer();

        return getNotiResponse(input);
    }

    private Optional<Response> getNotiResponse(final HandlerInput input) {
        String speechText;

        cl.sendStr("getnoti please");
        cl.readStr();
        cl.closeConnect();

        speechText = String.join(", ", cl.getNotiarray());

        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("NotiIntent", speechText)
                .build();
    }
}
