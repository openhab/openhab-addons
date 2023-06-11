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
package org.openhab.binding.sinope.internal.core.base;

import org.openhab.binding.sinope.internal.util.ByteUtil;
import org.openhab.binding.sinope.internal.util.CRC8;

/**
 * The Class SinopeFrame.
 *
 * @author Pascal Larin - Initial contribution
 */
abstract class SinopeFrame {

    /** The Constant PREAMBLE. */
    protected static final byte PREAMBLE = 0x55;

    /** The Constant FRAME_CTL. */
    protected static final byte FRAME_CTL = 0x00;

    /** The Constant PREAMBLE_SIZE. */
    protected static final int PREAMBLE_SIZE = 1;

    /** The Constant FRAME_CTL_SIZE. */
    protected static final int FRAME_CTL_SIZE = 1;

    /** The Constant CRC_SIZE. */
    protected static final int CRC_SIZE = 1;

    /** The Constant SIZE_SIZE. */
    protected static final int SIZE_SIZE = 2;

    /** The Constant COMMAND_SIZE. */
    protected static final byte COMMAND_SIZE = 2;

    /** The crc 8. */
    private final CRC8 crc8 = new CRC8();

    /** The internal payload. */
    protected byte[] internal_payload;

    /**
     * Gets the command.
     *
     * @return the command
     */
    protected abstract byte[] getCommand();

    /**
     * Gets the frame data.
     *
     * @return the frame data
     */
    protected abstract byte[] getFrameData();

    /**
     * Gets the payload.
     *
     * @return the payload
     */
    protected abstract byte[] getPayload();

    /**
     * Gets the crc8.
     *
     * @param buffer the buffer
     * @return the crc8
     */
    protected byte getCRC8(byte[] buffer) {
        crc8.reset();
        crc8.update(buffer, 0, buffer.length - 1);
        return (byte) (crc8.getValue());
    }

    /**
     * @see java.lang.Object#toString()
     */
    /*
     *
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        getPayload();
        sb.append(ByteUtil.toString(internal_payload));
        return sb.toString();
    }

    /**
     * Sets the payload.
     *
     * @param payload the new payload
     */
    public void setPayload(byte[] payload) {
        setInternal_payload(payload);
    }

    /**
     * Gets the internal payload.
     *
     * @return the internal payload
     */
    protected byte[] getInternal_payload() {
        return internal_payload;
    }

    /**
     * Sets the internal payload.
     *
     * @param internal_payload the new internal payload
     */
    protected void setInternal_payload(byte[] internal_payload) {
        this.internal_payload = internal_payload;
    }
}
