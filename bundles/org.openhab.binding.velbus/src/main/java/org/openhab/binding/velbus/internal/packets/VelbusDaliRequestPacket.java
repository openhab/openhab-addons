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

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;

/**
 * The {@link VelbusDaliRequestPacket} represents a Velbus packet that can be used to
 * request the values of the DALI channel(s) of the given Velbus module.
 *
 * @author Daniel Rosengarten - Initial contribution
 */
@NonNullByDefault
public class VelbusDaliRequestPacket extends VelbusPacket {
    private final byte GATEWAY_CONFIG = (byte) 0x01;

    private byte channel = ALL_DALI_CHANNELS;

    public VelbusDaliRequestPacket(byte address, byte channel) {
        super(address, PRIO_LOW);

        this.channel = (channel == ALL_CHANNELS) ? ALL_DALI_CHANNELS : channel;
    }

    public VelbusDaliRequestPacket(VelbusChannelIdentifier velbusChannelIdentifier) {
        super(velbusChannelIdentifier.getAddress(), PRIO_LOW);

        this.channel = velbusChannelIdentifier.getChannelByte();
    }

    @Override
    protected byte[] getDataBytes() {
        if (this.channel == ALL_DALI_CHANNELS) {
            return new byte[] { COMMAND_TEMP_SENSOR_SETTINGS_REQUEST, this.channel, GATEWAY_CONFIG };
        } else {
            return new byte[] { COMMAND_TEMP_SENSOR_SETTINGS_REQUEST, this.channel, GATEWAY_CONFIG,
                    DALI_SETTING_ACTUAL_LEVEL };
        }
    }
}
