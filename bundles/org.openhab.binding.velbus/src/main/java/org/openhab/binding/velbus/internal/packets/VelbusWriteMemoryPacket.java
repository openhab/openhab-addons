/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.COMMAND_WRITE_DATA_TO_MEMORY;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VelbusWriteMemoryPacket} represents a Velbus packet that can be used to
 * request a byte from the memory of the given Velbus module.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusWriteMemoryPacket extends VelbusPacket {
    private byte highMemoryAddress;
    private byte lowMemoryAddress;
    private byte data;

    public VelbusWriteMemoryPacket(byte address, int memoryAddress, byte data) {
        super(address, PRIO_LOW);

        this.highMemoryAddress = (byte) ((memoryAddress >> 8) & 0xFF);
        this.lowMemoryAddress = (byte) (memoryAddress & 0xFF);
        this.data = data;
    }

    @Override
    protected byte[] getDataBytes() {
        return new byte[] { COMMAND_WRITE_DATA_TO_MEMORY, highMemoryAddress, lowMemoryAddress, data };
    }
}
