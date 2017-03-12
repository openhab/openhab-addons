package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialResponse;

public class OnOffResponse implements LGSerialResponse {

    private int setId;

    private boolean success;

    private State state;

    public OnOffResponse(int setId, boolean success, String data) {
        this.setId = setId;
        this.success = success;

        if (success) {
            state = data.equals("01") ? OnOffType.ON : OnOffType.OFF;
        } else {
            state = new StringType(data);
        }
    }

    @Override
    public int getSetID() {
        return setId;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

}
