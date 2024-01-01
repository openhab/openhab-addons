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
package org.openhab.binding.lcn.internal.common;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an LCN group address.
 * Can be used as a key in maps.
 * Hash codes are guaranteed to be unique as long as {@link #isValid()} is true.
 *
 * @author Tobias Jüttner - Initial Contribution
 */
@NonNullByDefault
public class LcnAddrGrp extends LcnAddr implements Comparable<LcnAddrMod> {
    private final Logger logger = LoggerFactory.getLogger(LcnAddrGrp.class);
    private final int groupId;

    /**
     * Constructs a group address with (logical) segment id and group id.
     *
     * @param segId the segment id
     * @param grpId the group id
     */
    public LcnAddrGrp(int segId, int grpId) {
        super(segId);
        this.groupId = grpId;
    }

    /**
     * Gets the group id.
     *
     * @return the group id
     */
    public int getGroupId() {
        return this.groupId;
    }

    @Override
    public boolean isValid() {
        // segId:
        // 0 = Local, 1..2 = Not allowed (but "seen in the wild")
        // 3 = Broadcast, 4 = Status messages, 5..127, 128 = Segment-bus disabled (valid value)
        // grpId:
        // 3 = Broadcast, 4 = Status messages, 5..254
        return this.segmentId >= 0 && this.segmentId <= 128 && this.groupId >= 3 && this.groupId <= 254;
    }

    @Override
    public boolean isGroup() {
        return true;
    }

    @Override
    public int getId() {
        return this.groupId;
    }

    @Override
    public int hashCode() {
        // Reversing the bits helps to generate better balanced trees as ids tend to be "user-sorted"
        try {
            if (this.isValid()) {
                return ReverseNumber.reverseUInt8(this.groupId) << 8 + ReverseNumber.reverseUInt8(this.segmentId);
            }
        } catch (LcnException ex) {
            logger.warn("Could not calculate hash code");
        }
        return -1;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof LcnAddrGrp)) {
            return false;
        }
        return this.segmentId == ((LcnAddrGrp) obj).segmentId && this.groupId == ((LcnAddrGrp) obj).groupId;
    }

    @Override
    public int compareTo(LcnAddrMod other) {
        return this.hashCode() - other.hashCode();
    }

    @Override
    public String toString() {
        return this.isValid() ? String.format("S%03dG%03d", this.segmentId, this.groupId) : "Invalid";
    }
}
