package org.duckdns.gong.ask.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import org.duckdns.gong.ask.Client;

import java.util.ArrayList;
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
        String speechText;
        ArrayList<String> notifications;


        cl.enterServer();
        cl.sendStr("getnoti please");
        cl.readStr();
        cl.closeConnect();
        notifications = cl.getStrings();

        // // 저장된 알림이 없을 경우
        if(notifications.size()==0) {
            notifications.add("There is no notification");
        } else {
            // 저장된 알림의 갯수를 말해주는 문장을 배열리스트 처음에 추가
            notifications.add(0 ,String.format("There are %d notification", notifications.size()));
        }

        // 배열리스트에 저장된 알림들을 하나의 문자열로 합침, 사이에는 따옴표를 추가
        speechText = String.join(", ", notifications);

        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("NotiIntent", speechText)
                .build();
    }
}
