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

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.COMMAND_READ_MEMORY_BLOCK;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VelbusReadMemoryBlockPacket} represents a Velbus packet that can be used to
 * request a block of 4 bytes from the memory of the given Velbus module.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusReadMemoryBlockPacket extends VelbusPacket {
    private byte highMemoryAddress;
    private byte lowMemoryAddress;

    public VelbusReadMemoryBlockPacket(byte address, int memoryAddress) {
        super(address, PRIO_LOW);

        highMemoryAddress = (byte) ((memoryAddress >> 8) & 0xFF);
        lowMemoryAddress = (byte) (memoryAddress & 0xFF);
    }

    @Override
    protected byte[] getDataBytes() {
        return new byte[] { COMMAND_READ_MEMORY_BLOCK, highMemoryAddress, lowMemoryAddress };
    }
}
