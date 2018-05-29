package org.duckdns.gong.arduino;

import com.amazon.ask.Skill;
import com.amazon.ask.Skills;
import com.amazon.ask.SkillStreamHandler;

import org.duckdns.gong.arduino.handlers.LightsOffIntentHandler;
import org.duckdns.gong.arduino.handlers.LightsOnIntentHandler;
import org.duckdns.gong.arduino.handlers.SessionEndedRequestHandler;
import org.duckdns.gong.arduino.handlers.LaunchRequestHandler;
import org.duckdns.gong.arduino.handlers.HelpIntentHandler;

public class ArduinoStreamHandler extends SkillStreamHandler {
    private static Skill getSkill() {
        return Skills.standard()
                .addRequestHandlers(
                        new LightsOnIntentHandler(),
                        new LightsOffIntentHandler(),
                        new HelpIntentHandler(),
                        new LaunchRequestHandler(),
                        new SessionEndedRequestHandler())
                // Add your skill id below
                //.withSkillId("")
                .build();
    }

    public ArduinoStreamHandler() {
        super(getSkill());
    }
}