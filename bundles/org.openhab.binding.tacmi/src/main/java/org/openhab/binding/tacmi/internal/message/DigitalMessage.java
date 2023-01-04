/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.tacmi.internal.message;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class can be used to decode the digital values received in a messag and
 * also to create a new DigitalMessage used to send ON/OFF to a digital CAN
 * Input port
 *
 * @author Timo Wendt - Initial contribution
 * @author Christian Niessner - Ported to OpenHAB2
 */
@NonNullByDefault
public final class DigitalMessage extends Message {

    public DigitalMessage(byte[] raw) {
        super(raw);
    }

    /**
     * Create a new message to be sent to the CMI. It is only supported to use the
     * first port for each CAN node. This is due to the fact that all digital port
     * for the specific CAN node are send within a single message.
     */
    public DigitalMessage(byte canNode, byte podNr) {
        super(canNode, podNr);
    }

    /**
     * Get the state of the specified port number.
     *
     * @param portNumber
     * @return
     */
    public boolean getPortState(int portNumber) {
        return getBit(getValue(0), (portNumber - 1) % 16);
    }

    /**
     * Set the state of the specified port number.
     *
     * @param portNumber
     * @param value
     * @return
     */
    public boolean setPortState(int portNumber, boolean value) {
        short val = getValue(0);
        int bit = (1 << portNumber);
        if (value) {
            val |= bit;
        } else {
            val &= ~bit;
        }
        return setValue(0, val, 0);
    }

    /**
     * Read the specified bit from the short value holding the states of all 16
     * ports.
     *
     * @param portBits
     * @param portBit
     * @return
     */
    private boolean getBit(int portBits, int portBit) {
        int result = (portBits >> portBit) & 0x1;
        return result == 1 ? true : false;
    }

    /**
     * Check if message contains a value for the specified port number. portNumber
     * Digital messages are in POD 0 for 1-16 and POD 9 for 17-32
     *
     * @param portNumber - the portNumber in Range 1-32
     * @return
     */
    @Override
    public boolean hasPortnumber(int portNumber) {
        if (podNumber == 0 && portNumber <= 16) {
            return true;
        }
        if (podNumber == 9 && portNumber >= 17) {
            return true;
        }
        return false;
    }

    @Override
    public MessageType getType() {
        return MessageType.DIGITAL;
    }
}
