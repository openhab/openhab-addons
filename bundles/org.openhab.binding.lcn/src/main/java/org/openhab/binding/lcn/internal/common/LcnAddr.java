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
package org.openhab.binding.lcn.internal.common;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents an LCN address (module or group).
 *
 * @author Tobias Jüttner - Initial Contribution
 */
@NonNullByDefault
public abstract class LcnAddr {
    /**
     * The logical segment ID. When no segments are used, the ID is always 0. When segments are used and the module is
     * in the local segment, the ID is the local's segment ID.
     */
    protected final int segmentId;

    /**
     * Constructs an address with a (logical) segment id.
     *
     * @param segId the segment id
     */
    public LcnAddr(int segId) {
        this.segmentId = segId;
    }

    /**
     * Gets the (logical) segment id.
     *
     * @return the segment id
     */
    public int getSegmentId() {
        return this.segmentId;
    }

    /**
     * Gets the physical segment id ("local" segment replaced with 0).
     * Can be used to send data into the LCN bus.
     *
     * @param localSegegmentId the segment id of the local segment (managed by {@link Connection})
     * @return the physical segment id
     */
    public int getPhysicalSegmentId(int localSegegmentId) {
        return this.segmentId == localSegegmentId ? 0 : this.segmentId;
    }

    /**
     * Checks the address against the LCN specification for valid addresses.
     *
     * @return true if address is valid
     */
    public abstract boolean isValid();

    /**
     * Queries the concrete address type.
     *
     * @return true if address is a group address (module address otherwise)
     */
    public abstract boolean isGroup();

    /**
     * Gets the address' module or group id (discarding the concrete type).
     *
     * @return the module or group id
     */
    public abstract int getId();
}
