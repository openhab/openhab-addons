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
import java.nio.ByteBuffer;

import org.openhab.binding.sinope.internal.core.base.SinopeRequest;
import org.openhab.binding.sinope.internal.util.ByteUtil;

/**
 * The Class SinopeApiLoginRequest.
 *
 * @author Pascal Larin - Initial contribution
 */
public class SinopeApiLoginRequest extends SinopeRequest {

    /** The api key. */
    private byte[] apiKey;

    /** The id. */
    private byte[] id;

    /**
     * Instantiates a new sinope api login request.
     *
     * @param id the id
     * @param apiKey the api key
     */
    public SinopeApiLoginRequest(byte[] id, byte[] apiKey) {
        this.id = id;
        this.apiKey = apiKey;
    }

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeFrame#getCommand()
     */
    @Override
    protected byte[] getCommand() {
        return new byte[] { 0x01, 0x10 };
    }

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeFrame#getFrameData()
     */
    @Override
    protected byte[] getFrameData() {
        byte[] b = new byte[id.length + apiKey.length];

        ByteBuffer bb = ByteBuffer.wrap(b);

        bb.put(ByteUtil.reverse(id));
        bb.put(ByteUtil.reverse(apiKey));

        // System.out.println(toString(bb.array()));
        return bb.array();
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public byte[] getId() {
        return id;
    }

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeRequest#getReplyAnswer(java.io.InputStream)
     */
    @Override
    public SinopeApiLoginAnswer getReplyAnswer(InputStream r) throws IOException {
        return new SinopeApiLoginAnswer(r);
    }
}
