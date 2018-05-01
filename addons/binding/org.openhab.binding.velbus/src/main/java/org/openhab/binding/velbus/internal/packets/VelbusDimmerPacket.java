/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
