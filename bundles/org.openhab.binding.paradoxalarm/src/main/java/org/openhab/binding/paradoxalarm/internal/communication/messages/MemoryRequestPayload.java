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
package org.openhab.binding.paradoxalarm.internal.communication.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MemoryRequestPayload} Abstract class which contains common logic used in RAM and EPROM payload generation
 * classes.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public abstract class MemoryRequestPayload implements IPayload {

    private static final int BUFFER_LENGTH = 8;
    private static final short MESSAGE_START = (short) ((0x50 << 8) | 0x08);

    private final Logger logger = LoggerFactory.getLogger(MemoryRequestPayload.class);

    private final int address;
    private final byte bytesToRead;

    public MemoryRequestPayload(int address, byte bytesToRead) throws ParadoxException {
        if (bytesToRead < 1 || bytesToRead > 64) {
            throw new ParadoxException("Invalid bytes to read. Valid values are 1 to 64.");
        }

        this.address = address;
        this.bytesToRead = bytesToRead;

        logTraceHexFormatted("MessageStart: {}", MESSAGE_START);
    }

    protected abstract byte calculateControlByte();

    @Override
    public byte[] getBytes() {
        byte[] bufferArray = new byte[BUFFER_LENGTH];
        ByteBuffer buffer = ByteBuffer.wrap(bufferArray);
        buffer.order(ByteOrder.BIG_ENDIAN).putShort(MESSAGE_START);
        buffer.put(calculateControlByte());
        buffer.put((byte) 0x00);
        buffer.order(ByteOrder.BIG_ENDIAN).putShort((short) address);
        buffer.put(bytesToRead);
        buffer.put((byte) 0x00);
        return bufferArray;
    }

    protected int getAddress() {
        return address;
    }

    protected void logTraceHexFormatted(String text, int address) {
        logTraceOptional(text, "0x%02X,\t", address);
    }

    private void logTraceOptional(String text, String format, int address) {
        if (logger.isTraceEnabled()) {
            logger.trace("Address: {}", String.format(format, address));
        }
    }
}
