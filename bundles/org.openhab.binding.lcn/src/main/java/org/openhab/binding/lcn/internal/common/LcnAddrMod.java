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
package org.openhab.binding.lcn.internal.common;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an LCN module address.
 * Can be used as a key in maps.
 * Hash codes are guaranteed to be unique as long as {@link #isValid()} is true.
 *
 * @author Tobias Jüttner - Initial Contribution
 */
@NonNullByDefault
public class LcnAddrMod extends LcnAddr implements Comparable<LcnAddrMod> {
    private final Logger logger = LoggerFactory.getLogger(LcnAddrMod.class);
    private final int moduleId;

    /**
     * Constructs a module address with (logical) segment id and module id.
     *
     * @param segId the segment id
     * @param modId the module id
     */
    public LcnAddrMod(int segId, int modId) {
        super(segId);
        this.moduleId = modId;
    }

    /**
     * Gets the module id.
     *
     * @return the module id
     */
    public int getModuleId() {
        return this.moduleId;
    }

    @Override
    public boolean isValid() {
        // segId:
        // 0 = Local, 1..2 = Not allowed (but "seen in the wild")
        // 3 = Broadcast, 4 = Status messages, 5..127, 128 = Segment-bus disabled (valid value)
        // modId:
        // 1 = LCN-PRO, 2 = LCN-GVS/LCN-W, 4 = PCHK, 5..254, 255 = Unprog. (valid, but irrelevant here)
        return this.segmentId >= 0 && this.segmentId <= 128 && this.moduleId >= 1 && this.moduleId <= 254;
    }

    @Override
    public boolean isGroup() {
        return false;
    }

    @Override
    public int getId() {
        return this.moduleId;
    }

    @Override
    public int hashCode() {
        // Reversing the bits helps to generate better balanced trees as ids tend to be "user-sorted"
        try {
            if (this.isValid()) {
                return ReverseNumber.reverseUInt8(this.moduleId) << 8 + ReverseNumber.reverseUInt8(this.segmentId);
            }
        } catch (LcnException ex) {
            logger.warn("Could not calculate hash code");
        }
        return -1;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof LcnAddrMod)) {
            return false;
        }
        return this.segmentId == ((LcnAddrMod) obj).segmentId && this.moduleId == ((LcnAddrMod) obj).moduleId;
    }

    @Override
    public int compareTo(LcnAddrMod other) {
        return this.hashCode() - other.hashCode();
    }

    @Override
    public String toString() {
        return this.isValid() ? String.format("S%03dM%03d", this.segmentId, this.moduleId) : "Invalid";
    }
}
