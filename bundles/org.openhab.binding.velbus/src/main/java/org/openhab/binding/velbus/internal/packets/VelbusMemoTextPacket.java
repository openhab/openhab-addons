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
package org.openhab.binding.velbus.internal.packets;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.COMMAND_TEXT;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VelbusMemoTextPacket} represents a Velbus packet that can be used to
 * set the mode (comfort/day/night/safe) of the given Velbus thermostat module.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusMemoTextPacket extends VelbusPacket {
    private byte textStartPosition;
    private byte character1;
    private byte character2;
    private byte character3;
    private byte character4;
    private byte character5;

    public VelbusMemoTextPacket(byte address, byte textStartPosition, char[] text) {
        super(address, PRIO_LOW);

        if (textStartPosition < 0 || textStartPosition > 63) {
            throw new IllegalArgumentException("The text start position '" + textStartPosition
                    + "' is invalid, because it should be between 0 and 63.");
        }

        if (text.length > 5) {
            throw new IllegalArgumentException(
                    "The text '" + text.toString() + "' is invalid, because it cannot be longer than 5 characters.");
        }

        this.textStartPosition = textStartPosition;
        this.character1 = text.length > 0 ? (byte) text[0] : 0x00;
        this.character2 = text.length > 1 ? (byte) text[1] : 0x00;
        this.character3 = text.length > 2 ? (byte) text[2] : 0x00;
        this.character4 = text.length > 3 ? (byte) text[3] : 0x00;
        this.character5 = text.length > 4 ? (byte) text[4] : 0x00;
    }

    @Override
    protected byte[] getDataBytes() {
        return new byte[] { COMMAND_TEXT, 0x00, textStartPosition, character1, character2, character3, character4,
                character5 };
    }
}
