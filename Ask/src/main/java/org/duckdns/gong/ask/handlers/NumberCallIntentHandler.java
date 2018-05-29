package org.duckdns.gong.ask.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.DialogState;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Request;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.ui.SsmlOutputSpeech;

import org.duckdns.gong.ask.Client;
import java.util.Optional;
import static com.amazon.ask.request.Predicates.intentName;

public class NumberCallIntentHandler implements RequestHandler {
    private Client cl;

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("NumberCallIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        cl = new Client();
        Request request = input.getRequestEnvelope().getRequest();
        IntentRequest intentRequest = (IntentRequest) request;
        Intent intent = intentRequest.getIntent();
        DialogState dialogueState = intentRequest.getDialogState();
        String speechText;

        // 알렉사와 대화과정을 거치기 위하여
        if (dialogueState.equals(DialogState.STARTED)) {
            return input.getResponseBuilder()
                    .addDelegateDirective(intent)
                    .build();
        } else if (dialogueState.equals(DialogState.IN_PROGRESS)) {
            return input.getResponseBuilder()
                    .addDelegateDirective(intent)
                    .build();
        } else {
            cl.enterServer();
            speechText = "Ok. I will send call request.";
            // 전화발신 요청에 받는번호를 추가하여 서버로 전송
            cl.sendStr("req call " + intent.getSlots().get("Number").getValue());
            cl.closeConnect();

            return input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withSimpleCard("NumberCallIntent", speechText)
                    .build();
        }
    }
}
