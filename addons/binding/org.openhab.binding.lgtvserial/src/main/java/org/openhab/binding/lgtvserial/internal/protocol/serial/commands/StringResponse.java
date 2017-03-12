package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialResponse;

public class StringResponse implements LGSerialResponse {

    private int setId;

    private boolean success;

    private State state;

    public StringResponse(int setId, boolean success, String data) {
        this.setId = setId;
        this.success = success;
        state = new StringType(data);
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
