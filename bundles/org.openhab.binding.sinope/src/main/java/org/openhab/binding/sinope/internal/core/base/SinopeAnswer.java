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
package org.openhab.binding.sinope.internal.core.base;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.openhab.binding.sinope.internal.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SinopeAnswer.
 *
 * @author Pascal Larin - Initial contribution
 */
public abstract class SinopeAnswer extends SinopeRequest {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(SinopeAnswer.class);

    /**
     * Instantiates a new sinope answer.
     *
     * @param r the r
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public SinopeAnswer(InputStream r) throws IOException {
        byte[] header = new byte[SinopeFrame.PREAMBLE_SIZE + SinopeFrame.FRAME_CTL_SIZE + SinopeFrame.SIZE_SIZE];

        r.read(header, 0, header.length);

        if (header[0] != 0x55) {
            throw new IOException(String.format("Invalid header PREAMBLE: %02x", header[0]));
        }
        int startSizeIndex = SinopeFrame.PREAMBLE_SIZE + SinopeFrame.FRAME_CTL_SIZE;
        int endSizeIndex = startSizeIndex + SinopeFrame.SIZE_SIZE;
        byte[] sizeInByte = Arrays.copyOfRange(header, startSizeIndex, endSizeIndex);
        ByteBuffer bb = ByteBuffer.allocate(SIZE_SIZE);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(sizeInByte);
        short size = bb.getShort(0);

        byte[] payload = new byte[SinopeRequest.HEADER_COMMAND_CRC_SIZE + size];
        byte[] remain = new byte[size + 1];
        r.read(remain, 0, size + 1);
        bb = ByteBuffer.wrap(payload);
        bb.put(header);
        bb.put(remain);

        this.setInternal_payload(bb.array());
    }

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeRequest#getPayload()
     */
    /*
     *
     *
     * @see ca.tulip.sinope.core.internal.SinopeRequest#getPayload()
     */
    @Override
    public byte[] getPayload() {
        return getInternal_payload();
    }

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeRequest#getReplyAnswer(java.io.InputStream)
     */
    /*
     *
     *
     * @see ca.tulip.sinope.core.internal.SinopeRequest#getReplyAnswer(java.io.InputStream)
     */
    @Override
    public SinopeAnswer getReplyAnswer(InputStream r) {
        throw new NotSupportedException();
    }

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeFrame#getFrameData()
     */
    /*
     *
     *
     * @see ca.tulip.sinope.core.internal.SinopeFrame#getFrameData()
     */
    @Override
    protected final byte[] getFrameData() {
        byte[] b = this.getInternal_payload();
        int headerSize = SinopeFrame.PREAMBLE_SIZE + SinopeFrame.FRAME_CTL_SIZE + SinopeFrame.COMMAND_SIZE
                + SinopeFrame.SIZE_SIZE;
        return Arrays.copyOfRange(b, headerSize, b.length - SinopeFrame.CRC_SIZE);
    }

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeRequest#setInternal_payload(byte[])
     */
    @Override
    protected void setInternal_payload(byte[] internal_payload) {
        logger.debug("Answer  Frame: {}", ByteUtil.toString(internal_payload));
        super.setInternal_payload(internal_payload);
    }
}
