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

import org.openhab.binding.sinope.internal.core.appdata.SinopeAppData;
import org.openhab.binding.sinope.internal.core.base.SinopeDataRequest;

/**
 * The Class SinopeDataReadRequest.
 * 
 * @author Pascal Larin - Initial contribution
 */
public class SinopeDataWriteRequest extends SinopeDataRequest {

    /**
     * Instantiates a new sinope data read request.
     *
     * @param seq the seq
     * @param dstDeviceId the dst device id
     * @param appData the app data
     */
    public SinopeDataWriteRequest(byte[] seq, byte[] dstDeviceId, SinopeAppData appData) {
        super(seq, dstDeviceId, appData);
    }

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeFrame#getCommand()
     */
    @Override
    protected byte[] getCommand() {
        return new byte[] { 0x02, 0x44 };
    }

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeDataRequest#getReplyAnswer(java.io.InputStream)
     */
    @Override
    public SinopeDataReadAnswer getReplyAnswer(InputStream r) throws IOException {
        return new SinopeDataReadAnswer(r, this.getAppData());
    }
}
