/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.velbus.internal.packets;

import static org.openhab.binding.velbus.VelbusBindingConstants.COMMAND_SWITCH_BLIND_OFF;

import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;

/**
 * The {@link VelbusBlindOffPacket} represents a Velbus packet that can be used to
 * stop a moving blind.
 *
 * @author Cedric Boon - Initial contribution
 */
public class VelbusBlindOffPacket extends VelbusPacket {
    private byte channel;

    public VelbusBlindOffPacket(VelbusChannelIdentifier velbusChannelIdentifier) {
        super(velbusChannelIdentifier.getAddress(), PRIO_HI);

        this.channel = velbusChannelIdentifier.getChannelByte();
    }

    @Override
    protected byte[] getDataBytes() {
        return new byte[] { COMMAND_SWITCH_BLIND_OFF, channel };
    }

}
