package org.duckdns.gong.ask.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Request;
import com.amazon.ask.model.Response;
import org.duckdns.gong.ask.Client;
import java.util.Optional;
import static com.amazon.ask.request.Predicates.intentName;

public class MusicIntentHandler implements RequestHandler {
    private Client cl;

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("MusicIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        cl = new Client();
        Request request = input.getRequestEnvelope().getRequest();
        IntentRequest intentRequest = (IntentRequest) request;
        Intent intent = intentRequest.getIntent();
        String speechText;

        cl.enterServer();
        // 슬롯 값을 읽어서 기능을 구별하고 그 기능을 요청하는 문자열을 서버로 전송
        switch (intent.getSlots().get("Control").getValue()) {
            case "play":
                cl.sendStr("req music play");
                speechText = "Ok. I will send play music request.";
                break;
            case "pause":
                cl.sendStr("req music pause");
                speechText = "Ok. I will send pause music request.";
                break;
            case "next":
                cl.sendStr("req music next");
                speechText = "Ok. I will send next music request.";
                break;
            case "previous":
                cl.sendStr("req music previous");
                speechText = "Ok. I will send previous music request.";
                break;
            default:
                speechText="Your control request not supported";
        }
        cl.closeConnect();

        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("MusicIntent", speechText)
                .build();
    }
}

