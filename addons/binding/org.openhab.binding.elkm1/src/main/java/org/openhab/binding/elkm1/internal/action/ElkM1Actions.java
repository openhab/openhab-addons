package org.openhab.binding.elkm1.internal.action;

import org.eclipse.smarthome.model.script.engine.action.ActionDoc;
import org.openhab.binding.elkm1.internal.elk.ElkVoiceWords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElkM1Actions {
    private static final Logger logger = LoggerFactory.getLogger(ElkM1Actions.class);

    @ActionDoc(text = "Sends a command to the elk to speak a word")
    public static boolean elkM1SpeakWord(ElkVoiceWords words) {

        return true;
    }
}
