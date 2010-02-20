package org.openmuc.jrxtx;

import org.eclipse.smarthome.io.transport.serial.SerialPort;

/**
 * The parity.
 */
@SuppressWarnings("deprecation")
public enum Parity {
    /**
     * No parity bit will be sent with each data character at all.
     */
    NONE(SerialPort.PARITY_NONE),
    /**
     * An odd parity bit will be sent with each data character. I.e. will be set to 1 if the data character contains an
     * even number of bits set to 1.
     */
    ODD(SerialPort.PARITY_ODD),
    /**
     * An even parity bit will be sent with each data character. I.e. will be set to 1 if the data character contains an
     * odd number of bits set to 1.
     */
    EVEN(SerialPort.PARITY_EVEN),
    /**
     * A mark parity bit (i.e. always 1) will be sent with each data character.
     */
    MARK(SerialPort.PARITY_MARK),
    /**
     * A space parity bit (i.e. always 0) will be sent with each data character
     */
    SPACE(4),;
    private static final Parity[] VALUES = values();
    private int odlValue;

    private Parity(int oldValue) {
        this.odlValue = oldValue;
    }

    int getOldValue() {
        return this.odlValue;
    }

    static Parity forValue(int parity) {
        for (Parity p : VALUES) {
            if (p.odlValue == parity) {
                return p;
            }
        }

        // should not occur
        throw new RuntimeException("Error.");
    }
}
