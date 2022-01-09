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

import org.openhab.binding.sinope.internal.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SinopeRequest.
 *
 * @author Pascal Larin - Initial contribution
 */
public abstract class SinopeRequest extends SinopeFrame {

    /** The Constant HEADER_COMMAND_CRC_SIZE. */
    protected static final int HEADER_COMMAND_CRC_SIZE = SinopeFrame.PREAMBLE_SIZE + SinopeFrame.FRAME_CTL_SIZE
            + SinopeFrame.SIZE_SIZE + SinopeFrame.COMMAND_SIZE + SinopeFrame.CRC_SIZE;

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(SinopeRequest.class);

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeFrame#getPayload()
     */
    /*
     *
     *
     * @see ca.tulip.sinope.core.internal.SinopeFrame#getPayload()
     */
    @Override
    public byte[] getPayload() {
        if (getInternal_payload() == null) {
            byte[] command = getCommand();
            byte[] data = getFrameData();
            int len = HEADER_COMMAND_CRC_SIZE + data.length;
            byte[] buffer = new byte[len];
            ByteBuffer bb = ByteBuffer.wrap(buffer);
            bb.put(PREAMBLE);
            bb.put(FRAME_CTL);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            bb.putShort((short) (SinopeFrame.COMMAND_SIZE + data.length));

            bb.put(ByteUtil.reverse(command));
            bb.put(data);

            bb.put(getCRC8(bb.array()));

            setInternal_payload(bb.array());
        }
        return getInternal_payload();
    }

    /**
     * Gets the reply answer.
     *
     * @param r the r
     * @return the reply answer
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public abstract SinopeAnswer getReplyAnswer(InputStream r) throws IOException;

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeFrame#setInternal_payload(byte[])
     */
    @Override
    protected void setInternal_payload(byte[] internal_payload) {
        logger.debug("Request Frame: {}", ByteUtil.toString(internal_payload));
        super.setInternal_payload(internal_payload);
    }
}
