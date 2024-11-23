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

import org.openhab.binding.sinope.internal.core.base.SinopeAnswer;

/**
 * The Class SinopePingAnswer.
 * 
 * @author Pascal Larin - Initial contribution
 */
public class SinopePingAnswer extends SinopeAnswer {

    /**
     * Instantiates a new sinope ping answer.
     *
     * @param r the r
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public SinopePingAnswer(InputStream r) throws IOException {
        super(r);
    }

    /**
     * @see org.openhab.binding.sinope.internal.core.base.SinopeFrame#getCommand()
     */
    @Override
    protected byte[] getCommand() {
        return new byte[] { 0x00, 0x13 };
    }
}
