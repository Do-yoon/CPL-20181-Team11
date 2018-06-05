package org.duckdns.gong.ask.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.DialogState;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Request;
import com.amazon.ask.model.Response;

import org.duckdns.gong.ask.Client;

import java.time.Duration;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class TimerIntentHandler implements RequestHandler {
    private Client cl;

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("TimerIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        cl = new Client();
        Request request = input.getRequestEnvelope().getRequest();
        IntentRequest intentRequest = (IntentRequest) request;
        Intent intent = intentRequest.getIntent();
        String speechText;

        // 슬롯에 저장된 값을 읽어옴
        Duration duration = Duration.parse(intent.getSlots().get("Duration").getValue());

        cl.enterServer();
        cl.sendStr("req timer " + Long.toString(duration.getSeconds()));
        cl.closeConnect();
        speechText = "Ok. I will send timer request.";

        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("TimerIntent", speechText)
                .build();
    }
}
