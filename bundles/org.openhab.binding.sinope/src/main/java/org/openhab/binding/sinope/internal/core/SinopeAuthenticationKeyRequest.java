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
package org.openhab.binding.sinope.internal.core;

import java.io.IOException;
import java.io.InputStream;

import org.openhab.binding.sinope.internal.core.base.SinopeRequest;
import org.openhab.binding.sinope.internal.util.ByteUtil;

/**
 * The Class SinopeAuthenticationKeyRequest.
 *
 * @author Pascal Larin - Initial contribution
 */
public class SinopeAuthenticationKeyRequest extends SinopeRequest {

    /** The id. */
    private byte[] id;

    /**
     * Instantiates a new sinope authentication key request.
     *
     * @param id the id
     */
    public SinopeAuthenticationKeyRequest(byte[] id) {
        this.id = id;
    }

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeFrame#getCommand()
     */
    @Override
    protected byte[] getCommand() {
        return new byte[] { 0x01, 0x0a };
    }

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeFrame#getFrameData()
     */
    @Override
    protected byte[] getFrameData() {
        return ByteUtil.reverse(this.id);
    }

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeRequest#getReplyAnswer(java.io.InputStream)
     */
    @Override
    public SinopeAuthenticationKeyAnswer getReplyAnswer(InputStream r) throws IOException {
        return new SinopeAuthenticationKeyAnswer(r);
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public byte[] getId() {
        return id;
    }
}
