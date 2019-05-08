package org.openmuc.jrxtx;

import org.eclipse.smarthome.io.transport.serial.SerialPort;;

/**
 * The stop bits.
 */
@SuppressWarnings("deprecation")
public enum StopBits {
    /**
     * 1 stop bit will be sent at the end of every character.
     */
    STOPBITS_1(SerialPort.STOPBITS_1),
    /**
     * 1.5 stop bits will be sent at the end of every character
     */
    STOPBITS_1_5(SerialPort.STOPBITS_1_5),
    /**
     * 2 stop bits will be sent at the end of every character
     */
    STOPBITS_2(SerialPort.STOPBITS_2);

    private int odlValue;

    private StopBits(int oldValue) {
        this.odlValue = oldValue;
    }

    int getOldValue() {
        return this.odlValue;
    }
}
