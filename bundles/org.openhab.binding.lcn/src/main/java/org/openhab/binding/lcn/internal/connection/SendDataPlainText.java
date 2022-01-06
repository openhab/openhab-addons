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
package org.openhab.binding.lcn.internal.connection;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.binding.lcn.internal.common.PckGenerator;

/**
 * A plain text to be send to LCN-PCHK.
 *
 * @author Tobias Jüttner - Initial Contribution
 * @author Fabian Wolter - Migration to OH2
 */
@NonNullByDefault
class SendDataPlainText extends SendData {
    /** The text. */
    private final String text;

    /**
     * Constructor.
     *
     * @param text the text
     */
    SendDataPlainText(String text) {
        this.text = text;
    }

    /**
     * Gets the text.
     *
     * @return the text
     */
    String getText() {
        return this.text;
    }

    @Override
    boolean write(OutputStream buffer, int localSegId) throws IOException {
        buffer.write((this.text + PckGenerator.TERMINATION).getBytes(LcnDefs.LCN_ENCODING));
        return true;
    }

    @Override
    public String toString() {
        return text;
    }
}
