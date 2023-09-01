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

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velbus.internal.VelbusBindingConstants;
import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;

/**
 * The {@link VelbusSetColorPacket} represents a Velbus packet that can be used to
 * set the color of a channel on the DALI module.
 *
 * @author Daniel Rosengarten - Initial contribution
 */
@NonNullByDefault
public class VelbusSetColorPacket extends VelbusPacket {
    private byte channel;
    private byte[] data;

    public VelbusSetColorPacket(VelbusChannelIdentifier velbusChannelIdentifier) {
        super(velbusChannelIdentifier.getAddress(), PRIO_HI, false);

        this.channel = velbusChannelIdentifier.getChannelByte();
        this.data = new byte[] { VelbusBindingConstants.COMMAND_SET_COLOR, channel, VALUE_UNCHANGED, VALUE_UNCHANGED,
                VALUE_UNCHANGED, VALUE_UNCHANGED, VALUE_UNCHANGED };
    }

    public void setBrightness(byte brightness) {
        data[3] = brightness;
    }

    public void setColor(byte r, byte g, byte b) {
        data[4] = r;
        data[5] = g;
        data[6] = b;
    }

    public void setWhite(byte white) {
        data[7] = white;
    }

    @Override
    protected byte[] getDataBytes() {
        return data;
    }
}
