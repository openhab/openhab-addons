/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.velbus.internal.packets;

import static org.openhab.binding.velbus.VelbusBindingConstants.COMMAND_BLIND_POS;

import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;

/**
 * The {@link VelbusBlindPositionPacket} represents a Velbus packet that can be used to
 * set blinds to a given position.
 *
 * @author Cedric Boon - Initial contribution
 */
public class VelbusBlindPositionPacket extends VelbusPacket {
    private byte channel;
    private byte percentage;

    public VelbusBlindPositionPacket(VelbusChannelIdentifier velbusChannelIdentifier, byte percentage) {
        super(velbusChannelIdentifier.getAddress(), PRIO_HI);

        this.channel = velbusChannelIdentifier.getChannelByte();
        this.percentage = percentage;
    }

    @Override
    protected byte[] getDataBytes() {
        return new byte[] { COMMAND_BLIND_POS, channel, percentage };
    }

}
