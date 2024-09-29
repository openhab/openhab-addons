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

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.COMMAND_COUNTER_STATUS_REQUEST;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velbus.internal.VelbusChannelIdentifier;

/**
 * The {@link VelbusCounterStatusRequestPacket} represents a Velbus packet that can be used to
 * request the counter status of the given Velbus module.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusCounterStatusRequestPacket extends VelbusPacket {
    private byte channel;

    private final byte autosendTimeInterval = 0x00;

    public VelbusCounterStatusRequestPacket(VelbusChannelIdentifier velbusChannelIdentifier) {
        super(velbusChannelIdentifier.getAddress(), PRIO_LOW);

        this.channel = velbusChannelIdentifier.getChannelByte();
    }

    @Override
    protected byte[] getDataBytes() {
        return new byte[] { COMMAND_COUNTER_STATUS_REQUEST, channel, autosendTimeInterval };
    }
}
