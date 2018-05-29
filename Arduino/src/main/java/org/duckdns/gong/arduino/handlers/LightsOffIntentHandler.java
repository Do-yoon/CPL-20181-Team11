package org.duckdns.gong.arduino.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import org.duckdns.gong.arduino.Client;
import java.util.Optional;
import static com.amazon.ask.request.Predicates.intentName;

public class LightsOffIntentHandler implements RequestHandler {
    private Client cl = new Client();

    @Override
    public boolean canHandle(HandlerInput input) {
        // 인텐트 이름이 LedoffIntent일 경우 handle메소드를 실행
        return input.matches(intentName("LightsOffIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        String speechText;

        cl.enterServer();
        // 서버로 아두이노 LED를 꺼달라는 메세지를 전송
        cl.sendStr("arduino off");
        cl.closeConnect();
        // 알렉사의 대답을 결정
        speechText="Ok. I will send Lights off request.";

        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("LightsOffIntent", speechText)
                .build();
    }
}
