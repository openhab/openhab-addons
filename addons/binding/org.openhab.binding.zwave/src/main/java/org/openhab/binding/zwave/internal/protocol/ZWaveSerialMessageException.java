package org.openhab.binding.zwave.internal.protocol;

public class ZWaveSerialMessageException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -2106654578826723533L;

    ZWaveSerialMessageException(String reason) {
        super(reason);
    }
}
