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
import java.nio.BufferOverflowException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.common.LcnAddr;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.binding.lcn.internal.common.PckGenerator;

/**
 * A PCK command to be send to LCN-PCHK.
 * It is already encoded as bytes to allow different text-encodings (ANSI, UTF-8).
 *
 * @author Tobias Jüttner - Initial Contribution
 * @author Fabian Wolter - Migration to OH2
 */
@NonNullByDefault
class SendDataPck extends SendData {
    /** The target LCN address. */
    private final LcnAddr addr;

    /** true to acknowledge the command on receipt. */
    private final boolean wantsAck;

    /** PCK command (without address header) encoded as bytes. */
    private final byte[] data;

    /**
     * Constructor.
     *
     * @param addr the target LCN address
     * @param wantsAck true to claim receipt
     * @param data the PCK command encoded as bytes
     */
    SendDataPck(LcnAddr addr, boolean wantsAck, byte[] data) {
        this.addr = addr;
        this.wantsAck = wantsAck;
        this.data = data;
    }

    /**
     * Gets the PCK command.
     *
     * @return the PCK command encoded as bytes
     */
    byte[] getData() {
        return this.data;
    }

    @Override
    boolean write(OutputStream buffer, int localSegId) throws BufferOverflowException, IOException {
        buffer.write(PckGenerator.generateAddressHeader(this.addr, localSegId == -1 ? 0 : localSegId, this.wantsAck)
                .getBytes(LcnDefs.LCN_ENCODING));
        buffer.write(this.data);
        buffer.write(PckGenerator.TERMINATION.getBytes(LcnDefs.LCN_ENCODING));
        return true;
    }

    @Override
    public String toString() {
        return "Addr: " + addr + ": " + new String(data, 0, data.length, LcnDefs.LCN_ENCODING);
    }
}
