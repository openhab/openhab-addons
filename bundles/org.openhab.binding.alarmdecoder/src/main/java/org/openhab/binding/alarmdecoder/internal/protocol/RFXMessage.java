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

/**
 * The {@link RFXMessage} class represents a parsed RF zone (RFX) message.
 * Based partly on code from the OH1 alarmdecoder binding by Bernd Pfrommer.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class RFXMessage extends ADMessage {

    // Example: !RFX:0180036,80

    public static final int BIT_LOWBAT = 0x02;
    public static final int BIT_SUPER = 0x04;
    public static final int BIT_LOOP3 = 0x10;
    public static final int BIT_LOOP2 = 0x20;
    public static final int BIT_LOOP4 = 0x40;
    public static final int BIT_LOOP1 = 0x80;

    /** Address serial number */
    public final int serial;

    /** Message data */
    public final int data;

    public RFXMessage(String message) throws IllegalArgumentException {
        super(message);

        String[] topLevel = message.split(":");
        if (topLevel.length != 2) {
            throw new IllegalArgumentException("Multiple colons found in RFX message");
        }

        List<String> parts = splitMsg(topLevel[1]);

        if (parts.size() != 2) {
            throw new IllegalArgumentException("Invalid number of parts in RFX message");
        }

        try {
            serial = Integer.parseInt(parts.get(0));
            data = Integer.parseInt(parts.get(1), 16);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("RFX message contains invalid number: " + e.getMessage(), e);
        }
    }
}
