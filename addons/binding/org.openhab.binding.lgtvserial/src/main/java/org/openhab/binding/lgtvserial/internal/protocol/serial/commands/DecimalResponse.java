package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialResponse;

public class DecimalResponse implements LGSerialResponse {

    private int setId;

    private boolean success;

    private State state;

    public DecimalResponse(int setId, boolean success, DecimalType state) {
        this.setId = setId;
        this.success = success;
        this.state = state;
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
