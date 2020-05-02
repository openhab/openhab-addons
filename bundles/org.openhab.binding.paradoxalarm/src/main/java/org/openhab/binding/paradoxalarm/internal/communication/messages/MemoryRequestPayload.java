/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxException;
import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxRuntimeException;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EpromRequestPayload} Abstract class which contains common logic used in RAM and EPROM payload generation
 * classes.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public abstract class MemoryRequestPayload implements IPPacketPayload {

    private final Logger logger = LoggerFactory.getLogger(MemoryRequestPayload.class);

    private static final short MESSAGE_START = (short) ((0x50 << 8) | 0x08);

    private int address;
    private byte bytesToRead;

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
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            outputStream.write(ParadoxUtil.shortToByteArray(MESSAGE_START));
            outputStream.write(calculateControlByte());
            outputStream.write((byte) 0x00);

            outputStream.write(ParadoxUtil.shortToByteArray((short) address));

            outputStream.write(bytesToRead);

            // The bellow 0x00 is dummy which will be overwritten by the checksum
            outputStream.write(0x00);
            byte[] byteArray = outputStream.toByteArray();

            return byteArray;
        } catch (IOException e) {
            throw new ParadoxRuntimeException("Unable to create byte array stream.", e);
        }
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
