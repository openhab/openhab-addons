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
 * The {@link VelbusRelayPacket} represents a Velbus packet that can be used to
 * send commands to a Velbus relay module.
 *
 * @author Cedric Boon - Initial contribution
 */
public class VelbusRelayPacket extends VelbusPacket {
    private byte command;
    private byte channel;

    public VelbusRelayPacket(VelbusChannelIdentifier velbusChannelIdentifier, byte command) {
        super(velbusChannelIdentifier.getAddress(), PRIO_HI);

        this.channel = velbusChannelIdentifier.getChannelByte();
        this.command = command;
    }

    @Override
    protected byte[] getDataBytes() {
        return new byte[] { command, channel };
    }

}
