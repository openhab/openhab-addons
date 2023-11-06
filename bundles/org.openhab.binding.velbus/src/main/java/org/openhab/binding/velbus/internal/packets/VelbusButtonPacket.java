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
import org.openhab.binding.velbus.internal.VelbusBindingConstants;
import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;

/**
 * The {@link VelbusButtonPacket} represents a Velbus packet that can be used to
 * simulate a pushed button.
 *
 * @author Daniel Rosengarten - Initial contribution
 */
@NonNullByDefault
public class VelbusButtonPacket extends VelbusPacket {
    private byte channel;
    private byte[] data;

    public VelbusButtonPacket(VelbusChannelIdentifier velbusChannelIdentifier) {
        super(velbusChannelIdentifier.getAddress(), PRIO_HI, false);

        this.channel = velbusChannelIdentifier.getChannelByte();
        this.data = new byte[] { VelbusBindingConstants.COMMAND_PUSH_BUTTON_STATUS, (byte) 0x00, (byte) 0x00,
                (byte) 0x00 };
    }

    public void Pressed() {
        data = new byte[] { VelbusBindingConstants.COMMAND_PUSH_BUTTON_STATUS, channel, (byte) 0x00, (byte) 0x00 };
    }

    public void LongPressed() {
        data = new byte[] { VelbusBindingConstants.COMMAND_PUSH_BUTTON_STATUS, (byte) 0x00, (byte) 0x00, channel };
    }

    public void Released() {
        data = new byte[] { VelbusBindingConstants.COMMAND_PUSH_BUTTON_STATUS, (byte) 0x00, channel, (byte) 0x00 };
    }

    @Override
    protected byte[] getDataBytes() {
        return data;
    }
}
