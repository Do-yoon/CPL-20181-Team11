package org.duckdns.gong.ask;

import com.amazon.ask.Skill;
import com.amazon.ask.Skills;
import com.amazon.ask.SkillStreamHandler;
import org.duckdns.gong.ask.handlers.CancelandStopIntentHandler;
import org.duckdns.gong.ask.handlers.HelpIntentHandler;
import org.duckdns.gong.ask.handlers.MessageIntentHandler;
import org.duckdns.gong.ask.handlers.NameCallIntentHandler;
import org.duckdns.gong.ask.handlers.NotiIntentHandler;
import org.duckdns.gong.ask.handlers.NumberCallIntentHandler;
import org.duckdns.gong.ask.handlers.SessionEndedRequestHandler;
import org.duckdns.gong.ask.handlers.LaunchRequestHandler;
import org.duckdns.gong.ask.handlers.WolIntentHandler;

public class AndroidStreamHandler extends SkillStreamHandler {
    private static Skill getSkill() {
        return Skills.standard()
                .addRequestHandlers(
                        new NotiIntentHandler(),
                        new NameCallIntentHandler(),
                        new NumberCallIntentHandler(),
                        new MessageIntentHandler(),
                        new WolIntentHandler(),
                        new CancelandStopIntentHandler(),
                        new HelpIntentHandler(),
                        new LaunchRequestHandler(),
                        new SessionEndedRequestHandler())
                // Add your skill id below
                //.withSkillId("")
                .build();
    }

    public AndroidStreamHandler() {
        super(getSkill());
    }
}