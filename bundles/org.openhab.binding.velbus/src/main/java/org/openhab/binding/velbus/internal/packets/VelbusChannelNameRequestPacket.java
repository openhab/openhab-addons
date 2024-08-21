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

/**
 * The {@link VelbusChannelNameRequestPacket} represents a Velbus packet that can be used to
 * request the name of a given channel.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusChannelNameRequestPacket extends VelbusPacket {

    public VelbusChannelNameRequestPacket(byte address) {
        super(address, PRIO_LOW);
    }

    @Override
    protected byte[] getDataBytes() {
        return new byte[] { COMMAND_MODULE_NAME_REQUEST, ALL_CHANNELS };
    }
}
