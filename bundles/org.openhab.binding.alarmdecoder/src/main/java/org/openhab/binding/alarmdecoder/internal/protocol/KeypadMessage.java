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
package org.openhab.binding.alarmdecoder.internal.protocol;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link KeypadMessage} class represents a parsed keypad (KPM) message.
 * Based partly on code from the OH1 alarmdecoder binding by Bernd Pfrommer.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class KeypadMessage extends ADMessage {

    // Example: [00110011000000003A--],010,[f70700000010808c18020000000000],"ARMED ***STAY** ZONE BYPASSED "

    public static final int BIT_READY = 17;
    public static final int BIT_ARMEDAWAY = 16;
    public static final int BIT_ARMEDHOME = 15;
    public static final int BIT_BACKLIGHT = 14;
    public static final int BIT_PRORGAM = 13;
    public static final int BIT_BYPASSED = 9;
    public static final int BIT_ACPOWER = 8;
    public static final int BIT_CHIME = 7;
    public static final int BIT_ALARMOCCURRED = 6;
    public static final int BIT_ALARM = 5;
    public static final int BIT_LOWBAT = 4;
    public static final int BIT_DELAYOFF = 3;
    public static final int BIT_FIRE = 2;
    public static final int BIT_SYSFAULT = 1;
    public static final int BIT_PERIMETER = 0;

    public final String bitField;
    public final int numericCode;
    public final String rawData;
    public final String alphaMessage;
    public final int nbeeps;
    public final int status;

    private final int upper;
    private final int lower;

    public KeypadMessage(String message) throws IllegalArgumentException {
        super(message);
        List<String> parts = splitMsg(message.replace("!KPM:", ""));

        if (parts.size() != 4) {
            throw new IllegalArgumentException("Invalid number of parts in keypad message");
        }
        if (parts.get(0).length() != 22) {
            throw new IllegalArgumentException("Invalid field length in keypad message");
        }

        bitField = parts.get(0);
        rawData = parts.get(2);
        alphaMessage = parts.get(3).replaceAll("^\"|\"$", "");

        try {
            int numeric = 0;
            try {
                numeric = Integer.parseInt(parts.get(1));
            } catch (NumberFormatException e) {
                numeric = Integer.parseInt(parts.get(1), 16);
            }
            this.numericCode = numeric;

            this.upper = Integer.parseInt(parts.get(0).substring(1, 6), 2);
            this.nbeeps = Integer.parseInt(parts.get(0).substring(6, 7));
            this.lower = Integer.parseInt(parts.get(0).substring(7, 17), 2);
            this.status = ((upper & 0x1F) << 13) | ((nbeeps & 0x3) << 10) | lower;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("keypad msg contains invalid number: " + e.getMessage(), e);
        }
    }

    public int getZone() {
        return numericCode;
    }

    /**
     * Returns a string containing the keypad text
     */
    public String getText() {
        return alphaMessage;
    }

    /**
     * Returns the value of an individual bit in the status field
     *
     * @param bit status field bit to test
     * @return true if bit is 1, false if bit is 0
     */
    public boolean getStatus(int bit) {
        int v = (status >> bit) & 0x1;
        return (v == 0) ? false : true;
    }

    /**
     * Returns true if the READY status bit is set
     */
    public boolean panelClear() {
        return ((status & (1 << BIT_READY)) != 0);
    }

    /**
     * Returns a string containing the address mask of the message in hex
     */
    public String getAddressMask() {
        return rawData.substring(3, 11);
    }

    /**
     * Returns a long containing the address mask of the message
     */
    public long getLongAddressMask() {
        return Long.parseLong(getAddressMask(), 16);
    }

    /**
     * Compares two KeypadMessage objects
     *
     * @param obj KeypadMessage to compare against
     * @return true if messages are equal, false if obj is null, messages are not equal, or obj is not a KeypadMessage
     *         object.
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        } else if (this == obj) {
            return true;
        } else if (obj instanceof KeypadMessage other) {
            return this.message.equals(other.message);
        } else {
            return false;
        }
    }
}
