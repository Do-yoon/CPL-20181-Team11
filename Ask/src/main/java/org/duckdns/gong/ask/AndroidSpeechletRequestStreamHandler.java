package org.duckdns.gong.ask;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;
import java.util.HashSet;
import java.util.Set;

public class AndroidSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {
    private static final Set<String> supportedApplicationIds;

    static {
        supportedApplicationIds = new HashSet<String>();
        // supportedApplicationIds.add("[unique-value-here]");
    }

    public AndroidSpeechletRequestStreamHandler() {
        super(new AndroidSpeechlet(), supportedApplicationIds);
    }
}
