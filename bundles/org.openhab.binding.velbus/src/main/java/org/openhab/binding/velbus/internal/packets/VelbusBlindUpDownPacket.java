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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;

/**
 * The {@link VelbusBlindUpDownPacket} represents a Velbus packet that can be used to
 * move a blind up or down.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusBlindUpDownPacket extends VelbusPacket {
    private final byte timeoutHighByte = 0x00;
    private final byte timeoutMidByte = 0x00;
    private final byte timeoutLowByte = 0x00;

    private byte command;
    private byte channel;

    public VelbusBlindUpDownPacket(VelbusChannelIdentifier velbusChannelIdentifier, byte command) {
        super(velbusChannelIdentifier.getAddress(), PRIO_HI);

        this.channel = velbusChannelIdentifier.getChannelByte();
        this.command = command;
    }

    @Override
    protected byte[] getDataBytes() {
        return new byte[] { command, channel, timeoutHighByte, timeoutMidByte, timeoutLowByte };
    }
}
