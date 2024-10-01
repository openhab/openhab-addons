/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * The {@link EXPMessage} class represents a parsed zone (EXP or REL) message.
 * Based partly on code from the OH1 alarmdecoder binding by Bernd Pfrommer.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class EXPMessage extends ADMessage {

    // Example: !EXP:07,01,01
    // Example: !REL:12,01,01

    /** Address number */
    public final int address;

    /** Channel number */
    public final int channel;

    /** Message data */
    public final int data;

    public EXPMessage(String message) throws IllegalArgumentException {
        super(message);

        String[] topLevel = message.split(":");
        if (topLevel.length != 2) {
            throw new IllegalArgumentException("Multiple colons found in EXP message");
        }

        List<String> parts = splitMsg(topLevel[1]);

        if (parts.size() != 3) {
            throw new IllegalArgumentException("Invalid number of parts in EXP message");
        }

        try {
            address = Integer.parseInt(parts.get(0));
            channel = Integer.parseInt(parts.get(1));
            data = Integer.parseInt(parts.get(2));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("EXP message contains invalid number: " + e.getMessage(), e);
        }

        if ((data & ~0x1) != 0) {
            throw new IllegalArgumentException("zone status should only be 0 or 1");
        }
    }
}
