package org.openhab.binding.bluetooth;

public class BluetoothException extends Exception {

    public BluetoothException() {
        super();
    }

    public BluetoothException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BluetoothException(String message, Throwable cause) {
        super(message, cause);
    }

    public BluetoothException(String message) {
        super(message);
    }

    public BluetoothException(Throwable cause) {
        super(cause);
    }

}
