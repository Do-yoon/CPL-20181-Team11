package org.duckdns.gong.ask.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.DialogState;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Request;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;

import org.duckdns.gong.ask.Client;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class AlarmIntentHandler implements RequestHandler {
    private Client cl;

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AlarmIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        cl = new Client();
        Request request = input.getRequestEnvelope().getRequest();
        IntentRequest intentRequest = (IntentRequest) request;
        Intent intent = intentRequest.getIntent();
        DialogState dialogueState = intentRequest.getDialogState();
        // 슬롯 값들을 읽어옴
        Slot time = intent.getSlots().get("Time");
        Slot yesOrNo = intent.getSlots().get("Yesorno");
        Slot dayOfWeek = intent.getSlots().get("Dayofweek");
        String speechText = "Ok. I will send Alarm request.";

        // 알렉사와 대화과정을 거치기 위하여
        if (dialogueState.equals(DialogState.STARTED)) {
            return input.getResponseBuilder()
                    .addDelegateDirective(intent)
                    .build();
        } else if (dialogueState.equals(DialogState.IN_PROGRESS)) {
            switch(yesOrNo.getValue()==null ? "" : yesOrNo.getValue()) {
                case "yes":
                    // 사용자의 대답이 yes일 경우 대화를 계속 진행
                    return input.getResponseBuilder()
                            .addDelegateDirective(intent)
                            .build();
                case "no":
                    cl.enterServer();
                    // 서버로 알람 요청을 전송
                    cl.sendStr("req alarm " + time.getValue());
                    cl.closeConnect();
                    // 사용자의 대답이 no일 경우 대화를 종료
                    return input.getResponseBuilder()
                            .withSpeech(speechText)
                            .withShouldEndSession(true)
                            .withSimpleCard("AlarmIntent", speechText)
                            .build();
                default:
                    return input.getResponseBuilder()
                            .addDelegateDirective(intent)
                            .build();
            }
        } else {
            cl.enterServer();
            // 서버로 반복 알람 요청을 전송
            cl.sendStr("req repeatalarm " + time.getValue() + " " + dayOfWeek.getValue());
            cl.closeConnect();

            return input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withSimpleCard("AlarmIntent", speechText)
                    .build();
        }
    }
}
