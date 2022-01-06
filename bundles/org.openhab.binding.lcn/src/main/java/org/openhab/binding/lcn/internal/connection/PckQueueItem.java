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

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.common.LcnAddr;

/**
 * Holds data of one PCK command with the target address and the date when the item has been enqueued.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class PckQueueItem {
    private final Instant enqueued;
    private final LcnAddr addr;
    private final boolean wantsAck;
    private final byte[] data;

    public PckQueueItem(LcnAddr addr, boolean wantsAck, byte[] data) {
        this.enqueued = Instant.now();
        this.addr = addr;
        this.wantsAck = wantsAck;
        this.data = data;
    }

    /**
     * Gets the time when this message has been enqueued.
     *
     * @return the Instant
     */
    public Instant getEnqueued() {
        return enqueued;
    }

    /**
     * Gets the address of the destination LCN module.
     *
     * @return the address
     */
    public LcnAddr getAddr() {
        return addr;
    }

    /**
     * Checks whether an Ack is requested.
     *
     * @return true, if an Ack is requested
     */
    public boolean isWantsAck() {
        return wantsAck;
    }

    /**
     * Gets the raw PCK message to be sent.
     *
     * @return message as ByteBuffer
     */
    public byte[] getData() {
        return data;
    }
}
