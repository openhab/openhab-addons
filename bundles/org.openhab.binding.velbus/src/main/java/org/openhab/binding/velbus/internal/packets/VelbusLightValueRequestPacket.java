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

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.COMMAND_LIGHT_VALUE_REQUEST;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VelbusLightValueRequestPacket} represents a Velbus packet that can be used to
 * request the value of the light sensor of the given Velbus module.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusLightValueRequestPacket extends VelbusPacket {

    private final byte autosendTimeInterval = 0x00;

    public VelbusLightValueRequestPacket(byte address) {
        super(address, PRIO_LOW);
    }

    @Override
    protected byte[] getDataBytes() {
        return new byte[] { COMMAND_LIGHT_VALUE_REQUEST, autosendTimeInterval };
    }
}
