package org.openhab.binding.bluetooth.bluez;

import org.openhab.binding.bluetooth.BluetoothException;

public class BluezException extends BluetoothException {

    public BluezException() {
        super();
    }

    public BluezException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BluezException(String message, Throwable cause) {
        super(message, cause);
    }

    public BluezException(String message) {
        super(message);
    }

    public BluezException(Throwable cause) {
        super(cause);
    }

}
