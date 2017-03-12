package org.openhab.binding.lgtvserial.internal.protocol.serial;

import org.eclipse.smarthome.core.types.State;

public interface LGSerialResponse {

    int getSetID();

    State getState();

    boolean isSuccess();

}
