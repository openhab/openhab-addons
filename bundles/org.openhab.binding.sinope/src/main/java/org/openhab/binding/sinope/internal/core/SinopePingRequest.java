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

/**
 * The Class SinopePingRequest.
 * 
 * @author Pascal Larin - Initial contribution
 */
public class SinopePingRequest extends SinopeRequest {

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeFrame#getCommand()
     */
    @Override
    protected byte[] getCommand() {
        return new byte[] { 0x00, 0x12 };
    }

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeFrame#getFrameData()
     */
    @Override
    protected byte[] getFrameData() {
        return new byte[0];
    }

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeRequest#getReplyAnswer(java.io.InputStream)
     */
    @Override
    public SinopePingAnswer getReplyAnswer(InputStream r) throws IOException {
        return new SinopePingAnswer(r);
    }
}
