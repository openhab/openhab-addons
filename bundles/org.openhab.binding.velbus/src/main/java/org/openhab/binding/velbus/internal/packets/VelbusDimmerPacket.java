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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;

/**
 * The {@link VelbusDimmerPacket} represents a Velbus packet that can be used to
 * dim a light to a given percentage.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusDimmerPacket extends VelbusPacket {
    private static final byte FIRST_GENERATION_DEVICE_FASTEST_DIMSPEED_HIGH_BYTE = (byte) 0xFF;
    private static final byte FIRST_GENERATION_DEVICE_FASTEST_DIMSPEED_LOW_BYTE = (byte) 0xFF;

    private Boolean isFirstGenerationDevice;
    private byte command;
    private byte channel;
    private byte percentage;
    private int dimspeed;

    public VelbusDimmerPacket(VelbusChannelIdentifier velbusChannelIdentifier, byte command, byte percentage,
            int dimspeed, Boolean isFirstGenerationDevice) {
        super(velbusChannelIdentifier.getAddress(), PRIO_HI);

        this.channel = velbusChannelIdentifier.getChannelByte();
        this.command = command;
        this.percentage = percentage;
        this.dimspeed = dimspeed;
        this.isFirstGenerationDevice = isFirstGenerationDevice;
    }

    @Override
    protected byte[] getDataBytes() {
        byte dimspeedHighByte = (byte) ((dimspeed & 0xFF00) / 0x100);
        byte dimspeedLowByte = (byte) (dimspeed & 0xFF);

        if (dimspeed == 0 && isFirstGenerationDevice) {
            dimspeedHighByte = FIRST_GENERATION_DEVICE_FASTEST_DIMSPEED_HIGH_BYTE;
            dimspeedLowByte = FIRST_GENERATION_DEVICE_FASTEST_DIMSPEED_LOW_BYTE;
        }

        return new byte[] { command, channel, percentage, dimspeedHighByte, dimspeedLowByte };
    }
}
