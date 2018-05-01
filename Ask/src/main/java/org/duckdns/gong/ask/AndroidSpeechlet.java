package org.duckdns.gong.ask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AndroidSpeechlet implements SpeechletV2 {
    private static final Logger log = LoggerFactory.getLogger(AndroidSpeechlet.class);
    private Client cl;

    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
        log.info("onSessionStarted requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
        cl=new Client();
    }

    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
        log.info("onLaunch requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        IntentRequest request = requestEnvelope.getRequest();
        Session session = requestEnvelope.getSession();
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(), session);

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        /* 스킬에 만들어 놓은 Intent들을 이름으로 구별 하여 그에 맞는 반응을 실행한다 */
        if ("CallIntent".equals(intentName)) {
            cl.enterServer();
            return getCallResponse(intent, session);
        } else if ("NotiIntent".equals(intentName)) {
            cl.enterServer();
            return getNotiResponse();
        } else if ("MessageIntent".equals(intentName)) {
            cl.enterServer();
            return getMessageResponse(intent, session);
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            return getHelpResponse();
        } else {
            return getAskResponse("Call", "This is unsupported.  Please try something else.");
        }
    }

    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
        log.info("onSessionEnded requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
    }

    private SpeechletResponse getWelcomeResponse() {
        String speechText = "Welcome to the Alexa Skills Kit";
        return getAskResponse("Call", speechText);
    }

    /* 보이스로 전화 요청이 왔을 경우 반응하는 메소드이다 */
    private SpeechletResponse getCallResponse(final Intent intent, final Session session) {
        Map<String, Slot> slots = intent.getSlots();
        Slot nameslot = slots.get("Name");
        Slot numberslot = slots.get("Number");
        String speechText, name, number;

        if (nameslot != null) {
            name = nameslot.getValue();
            speechText = String.format("Ok. I will send request that call %s",name);
            cl.sendStr("req call "+name);
            cl.closeConnect();
        } else if (numberslot != null) {
            number = numberslot.getValue();
            speechText = String.format("Ok. I will send request that call %s",number);
            cl.sendStr("req call "+number);
            cl.closeConnect();
        } else {
            speechText = "I'm not sure what name, please try again";
        }

        SimpleCard card = getSimpleCard("Call", speechText);
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    /* 보이스로 알림 출력 요청이 왔을 경우 반응하는 메소드이다 */
    private SpeechletResponse getNotiResponse() {
        List<String> notiarray;
        String speechText;

        cl.sendStr("getnoti please");
        cl.readStr();
        cl.closeConnect();
        speechText=String.join(", ",cl.getNotiarray());         // Arraylist에 있는 모든 알림들을 하나의 문자열로 합친다

        SimpleCard card = getSimpleCard("Noti", speechText);
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    /* 보이스로 메시지 전송 요청이 왔을 경우 반응하는 메소드이다 */
    private SpeechletResponse getMessageResponse(final Intent intent, final Session session) {
        Map<String, Slot> slots = intent.getSlots();
        Slot nameslot = slots.get("Name");
        Slot numberslot = slots.get("Number");
        Slot contentslot = slots.get("Content");
        String speechText, name, number, content;

        if (nameslot != null) {
            name = nameslot.getValue();
            content=contentslot.getValue();
            speechText = String.format("Ok. I will send request that message %s",name);
            cl.sendStr("req message "+name+" "+content);
            cl.closeConnect();
        } else if (numberslot != null) {
            number = numberslot.getValue();
            content = contentslot.getValue();
            speechText = String.format("Ok. I will send request that message %s",number);
            cl.sendStr("req message "+number+" "+content);
            cl.closeConnect();
        } else {
            speechText = "I'm not sure what name, please try again";
        }

        SimpleCard card = getSimpleCard("Message", speechText);
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    private SpeechletResponse getHelpResponse() {
        String speechText = "You can say call name";
        return getAskResponse("Call", speechText);
    }

    private SimpleCard getSimpleCard(String title, String content) {
        SimpleCard card = new SimpleCard();
        card.setTitle(title);
        card.setContent(content);

        return card;
    }

    private PlainTextOutputSpeech getPlainTextOutputSpeech(String speechText) {
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return speech;
    }

    private Reprompt getReprompt(OutputSpeech outputSpeech) {
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(outputSpeech);

        return reprompt;
    }

    private SpeechletResponse getAskResponse(String cardTitle, String speechText) {
        SimpleCard card = getSimpleCard(cardTitle, speechText);
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);
        Reprompt reprompt = getReprompt(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }
}
