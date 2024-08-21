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
package org.openhab.binding.velbus.internal.packets;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.COMMAND_BLIND_POS;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;

/**
 * The {@link VelbusBlindPositionPacket} represents a Velbus packet that can be used to
 * set blinds to a given position.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
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
