package org.duckdns.gong.ask.handlers;

import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.DialogState;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Request;
import com.amazon.ask.model.Response;
import org.duckdns.gong.ask.Client;

import java.util.ArrayList;
import java.util.Optional;
import static com.amazon.ask.request.Predicates.intentName;

public class CalendarIntentHandler implements RequestHandler {
    private Client cl;

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("CalendarIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        cl = new Client();
        ArrayList<String> calEvents;
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
            // 서버로 캘린더 일정을 요청
            cl.sendStr("req calendar " + intent.getSlots().get("Date").getValue());
            cl.readStr();
            cl.closeConnect();
            calEvents = cl.getStrings();

            // 저장된 이벤트가 없을 경우
            if(calEvents.size()==0) {
                calEvents.add("There is no schedule");
            } else {
                // 저장된 이벤트의 갯수를 말해주는 문장을 배열리스트 처음에 추가
                calEvents.add(0 ,String.format("There are %d schedule", calEvents.size()));
            }
            // 배열리스트에 저장된 캘린더 이벤트들을 하나의 문자열로 합침, 사이에는 따옴표를 추가
            speechText = String.join(", ", calEvents);

            return input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withSimpleCard("CalendarIntent", speechText)
                    .build();
        }
    }
}
