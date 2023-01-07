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
package org.openhab.binding.lcn.internal.connection;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base class for a packet to be send to LCN-PCHK.
 *
 * @author Tobias Jüttner - Initial Contribution
 * @author Fabian Wolter - Migration to OH2
 */
@NonNullByDefault
public abstract class SendData {
    /**
     * Writes the packet's data into the given buffer.
     * Called right before the packet is actually sent to LCN-PCHK.
     *
     * @param buffer the target buffer
     * @param localSegId the local segment id
     * @return true if everything was set-up correctly and data was written
     * @throws IOException if an I/O error occurs
     */
    abstract boolean write(OutputStream buffer, int localSegId) throws IOException;
}
