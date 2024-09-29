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
package org.openhab.binding.sinope.internal.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.openhab.binding.sinope.internal.core.base.SinopeAnswer;
import org.openhab.binding.sinope.internal.util.ByteUtil;

/**
 * The Class SinopeAuthenticationKeyAnswer.
 * 
 * @author Pascal Larin - Initial contribution
 */
public class SinopeAuthenticationKeyAnswer extends SinopeAnswer {

    /** The Constant STATUS_SIZE. */
    protected static final int STATUS_SIZE = 1;

    /** The Constant BACKOFF_SIZE. */
    protected static final int BACKOFF_SIZE = 2;

    /** The Constant API_KEY_SIZE. */
    protected static final int API_KEY_SIZE = 8;

    /**
     * Instantiates a new sinope authentication key answer.
     *
     * @param r the r
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public SinopeAuthenticationKeyAnswer(InputStream r) throws IOException {
        super(r);
    }

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeFrame#getCommand()
     */
    @Override
    protected byte[] getCommand() {
        return new byte[] { 0x01, 0x11 };
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public int getStatus() {
        byte[] b = this.getFrameData();
        return b[0];
    }

    /**
     * Gets the backoff.
     *
     * @return the backoff
     */
    public byte[] getBackoff() {
        byte[] b = this.getFrameData();
        b = Arrays.copyOfRange(b, STATUS_SIZE, STATUS_SIZE + BACKOFF_SIZE);
        return b;
    }

    /**
     * Gets the api key.
     *
     * @return the api key
     */
    public byte[] getApiKey() {
        byte[] b = this.getFrameData();
        b = Arrays.copyOfRange(b, STATUS_SIZE + BACKOFF_SIZE, STATUS_SIZE + BACKOFF_SIZE + API_KEY_SIZE);
        return ByteUtil.reverse(b);
    }

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeFrame#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(String.format("\nData: %s", ByteUtil.toString(getFrameData())));
        sb.append(String.format("\n\tStatus: 0x%02X ", getStatus()));
        sb.append(String.format("\n\tApi Key: %s", ByteUtil.toString(getApiKey())));
        sb.append(String.format("\n\tBackoff: %s", ByteUtil.toString(getBackoff())));
        return sb.toString();
    }
}
