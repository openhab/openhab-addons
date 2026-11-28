/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
        return b.length >= STATUS_SIZE ? b[0] & 0xFF : -1;
    }

    /**
     * Gets the backoff.
     *
     * @return the backoff
     */
    public byte[] getBackoff() {
        byte[] b = this.getFrameData();
        if (b.length < STATUS_SIZE + BACKOFF_SIZE) {
            return new byte[0];
        }
        return Arrays.copyOfRange(b, STATUS_SIZE, STATUS_SIZE + BACKOFF_SIZE);
    }

    /**
     * Gets the api key.
     *
     * @return the api key
     */
    public byte[] getApiKey() {
        byte[] b = this.getFrameData();
        if (b.length < STATUS_SIZE + BACKOFF_SIZE + API_KEY_SIZE) {
            return new byte[0];
        }
        b = Arrays.copyOfRange(b, STATUS_SIZE + BACKOFF_SIZE, STATUS_SIZE + BACKOFF_SIZE + API_KEY_SIZE);
        return ByteUtil.reverse(b);
    }

    @Override
    protected void setInternal_payload(byte[] internal_payload) {
        // Avoid logging the raw authentication key payload
        this.internal_payload = internal_payload;
    }

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeFrame#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SinopeAuthenticationKeyAnswer [status=0x%02X, backoff=%s, apiKey=%s]", getStatus(),
                ByteUtil.toString(getBackoff()).trim(), getApiKey().length > 0 ? "***" : "<empty>"));
        return sb.toString();
    }
}
