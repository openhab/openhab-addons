package org.openhab.binding.isy.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.State;

public class StatusChangeHandler {

    private String mChannel;

    public StatusChangeHandler(String channel) {
        this.mChannel = channel;
    }

    public static State statusValuetoState(int updateValue) {
        State returnValue;
        if (updateValue > 0) {
            returnValue = new PercentType(updateValue * 100 / 255);
        } else {
            returnValue = OnOffType.OFF;
        }
        return returnValue;

    }
}
