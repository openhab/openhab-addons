/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * Format of analog messages is as follows:
 * 1 2 3 4 5 6 7 8 9 10 11 12 13 14
 * 0 1 2 3 4 5 6 7 8 9 10 11 12 13
 * canNode 1|2|3|4 1.lower 1.upper 2.lower 2.upper 3.lower 3.upper 4.lower 4.upper 1.type 2.type 3.type 4.type
 *
 * possible values for type according to the documentation are 1 to 21.
 *
 * The documentation says for the types:
 *
 * 1: Degree Celsius
 * 2: Watts per square meter
 * 3: liters per hour
 * 4: seconds
 * 5: minutes
 * 6: liters per pulse
 * 7: Kelvin
 * 8: Percent
 * 9: Kilowatt
 * 10: Megawatthours
 * 11: Kilowatthours
 * 12: Volt
 * 13: Milliampere
 * 14: hours
 * 15: days
 * 16: pulses
 * 17: Kiloohm
 * 18: Kilometers per hour
 * 19: Hertz
 * 20: liters per minute
 * 21: bar
 *
 * However, reality shows that the documentation is partly not accurate. An UVR1611 device uses:
 *
 * 1: Degree Celsius
 * 4: Seconds
 * 10: Kilowatt
 * 11: Megawatthours
 * 12: Kilowatthours
 *
 * so we don't rely on the documentation.
 *
 * This class can be used to decode the analog values received in a message and
 * also to create a new AnalogMessage used to send analog values to an analog
 * CAN Input port. Creation of new message is not implemented so far.
 *
 * @author Timo Wendt - Initial contribution
 * @author Wolfgang Klimt - improvements
 * @author Christian Niessner - Ported to OpenHAB2
 */
@NonNullByDefault
public final class AnalogMessage extends Message {

    /**
     * Used to parse the data received from the CMI.
     *
     * @param raw
     */
    public AnalogMessage(byte[] raw) {
        super(raw);
    }

    /**
     * Create a new message to be sent to the CMI. It is only supported to use
     * the first port for each podNumber.
     */
    public AnalogMessage(byte canNode, byte podNumber) {
        super(canNode, podNumber);
    }

    /**
     * Get the value for the specified port number.
     *
     * @param portNumber
     * @return
     */
    public AnalogValue getAnalogValue(int portNumber) {
        // Get the internal index for portNumber within the message
        int idx = (portNumber - 1) % 4;
        return new AnalogValue(this.getValue(idx), getMeasureType(idx));
    }

    /**
     * Check if message contains a value for the specified port number. It
     * doesn't matter though if the port has a value of 0.
     *
     * @param portNumber
     * @return
     */
    @Override
    public boolean hasPortnumber(int portNumber) {
        return (portNumber - 1) / 4 == podNumber - 1;
    }

    @Override
    public MessageType getType() {
        return MessageType.ANALOG;
    }
}
