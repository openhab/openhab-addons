/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;

/**
 * The {@link VelbusDimmerPacket} represents a Velbus packet that can be used to
 * dim a light to a given percentage.
 *
 * @author Cedric Boon - Initial contribution
 */
public class VelbusDimmerPacket extends VelbusPacket {
    private final byte dimspeedHighByte = 0x00;
    private final byte dimspeedLowByte = 0x00;

    private byte command;
    private byte channel;
    private byte percentage;

    public VelbusDimmerPacket(VelbusChannelIdentifier velbusChannelIdentifier, byte command, byte percentage) {
        super(velbusChannelIdentifier.getAddress(), PRIO_HI);

        this.channel = velbusChannelIdentifier.getChannelByte();
        this.command = command;
        this.percentage = percentage;
    }

    @Override
    protected byte[] getDataBytes() {
        return new byte[] { command, channel, percentage, dimspeedHighByte, dimspeedLowByte };
    }
}
