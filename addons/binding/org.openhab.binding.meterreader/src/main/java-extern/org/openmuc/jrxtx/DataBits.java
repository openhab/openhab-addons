package org.openmuc.jrxtx;

import org.eclipse.smarthome.io.transport.serial.SerialPort;;

/**
 * The data bits.
 */
@SuppressWarnings("deprecation")
public enum DataBits {
    /**
     * 5 data bits will be used for each character.
     */
    DATABITS_5(SerialPort.DATABITS_5),
    /**
     * 6 data bits will be used for each character.
     */
    DATABITS_6(SerialPort.DATABITS_6),
    /**
     * 8 data bits will be used for each character.
     */
    DATABITS_7(SerialPort.DATABITS_7),
    /**
     * 8 data bits will be used for each character.
     */
    DATABITS_8(SerialPort.DATABITS_8),;
    private int odlValue;

    private DataBits(int oldValue) {
        this.odlValue = oldValue;
    }

    int getOldValue() {
        return this.odlValue;
    }
}
