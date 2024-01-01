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

/**
 * The {@link VelbusThermostatOperatingModePacket} represents a Velbus packet that can be used to
 * set the operating mode (heating/cooling) of the given Velbus thermostat module.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusThermostatOperatingModePacket extends VelbusPacket {

    private byte commandByte;

    public VelbusThermostatOperatingModePacket(byte address, byte commandByte) {
        super(address, PRIO_LOW);

        this.commandByte = commandByte;
    }

    @Override
    protected byte[] getDataBytes() {
        return new byte[] { commandByte, 0x00 };
    }
}
