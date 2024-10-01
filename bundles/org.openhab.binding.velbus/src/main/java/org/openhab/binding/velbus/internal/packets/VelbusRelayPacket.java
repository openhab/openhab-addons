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
import org.openhab.binding.velbus.internal.VelbusModuleAddress;

/**
 * The {@link VelbusRelayPacket} represents a Velbus packet that can be used to
 * send commands to a Velbus relay module.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusRelayPacket extends VelbusPacket {
    private byte command;
    private byte channel;

    public VelbusRelayPacket(VelbusChannelIdentifier velbusChannelIdentifier, byte command) {
        super(velbusChannelIdentifier.getAddress(), PRIO_HI);

        this.channel = velbusChannelIdentifier.getChannelByte();
        this.command = command;
    }

    public VelbusRelayPacket(VelbusModuleAddress velbusModuleAddress, byte command) {
        super(velbusModuleAddress.getAddress(), PRIO_HI);

        this.channel = ALL_CHANNELS;
        this.command = command;
    }

    @Override
    protected byte[] getDataBytes() {
        return new byte[] { command, channel };
    }
}
