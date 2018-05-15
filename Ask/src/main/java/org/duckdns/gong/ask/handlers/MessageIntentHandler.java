package org.duckdns.gong.ask.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.DialogState;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Request;
import com.amazon.ask.model.Response;
import org.duckdns.gong.ask.Client;
import java.util.Optional;
import static com.amazon.ask.request.Predicates.intentName;

public class MessageIntentHandler implements RequestHandler {
    private Client cl = new Client();

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("MessageIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        Request request = input.getRequestEnvelope().getRequest();
        IntentRequest intentRequest = (IntentRequest) request;
        Intent intent = intentRequest.getIntent();
        String speechText, phrase, name, content;
        DialogState dialogueState = intentRequest.getDialogState();

        if (dialogueState.equals(DialogState.STARTED)) {
            return input.getResponseBuilder()
                    .addDelegateDirective(intent)
                    .build();
        } else if (!(dialogueState.equals(DialogState.COMPLETED))) {
            return input.getResponseBuilder()
                    .addDelegateDirective(intent)
                    .build();
        } else {
            phrase = intent.getSlots().get("Content").getValue();
            name = phrase.split(" ")[0];
            content = phrase.substring(phrase.indexOf(" ")+1);
            cl.enterServer();
            speechText = "Ok. I will send message request.";
            cl.sendStr("req message " + name + " " + content);
            cl.closeConnect();

            return input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withSimpleCard("MessageIntent", speechText)
                    .build();
        }
    }
}
