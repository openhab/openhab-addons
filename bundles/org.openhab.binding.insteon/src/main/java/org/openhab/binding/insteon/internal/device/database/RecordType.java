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
package org.openhab.binding.insteon.internal.device.database;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.utils.BinaryUtils;

/**
 * The {@link RecordType} represents an Insteon all-link record type
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class RecordType {
    private static final int BIT_ACTIVE = 7;
    private static final int BIT_CONTROLLER = 6;
    private static final int BIT_HIGH_WATER_MARK = 1;

    private final int flags;

    public RecordType(int flags) {
        this.flags = flags;
    }

    public int getFlags() {
        return flags;
    }

    public boolean isActive() {
        return BinaryUtils.isBitSet(flags, BIT_ACTIVE);
    }

    public boolean isController() {
        return BinaryUtils.isBitSet(flags, BIT_CONTROLLER);
    }

    public boolean isResponder() {
        return !BinaryUtils.isBitSet(flags, BIT_CONTROLLER);
    }

    public boolean isHighWaterMark() {
        return !BinaryUtils.isBitSet(flags, BIT_HIGH_WATER_MARK);
    }

    @Override
    public String toString() {
        String s;
        if (isHighWaterMark()) {
            s = "LAST";
        } else if (!isActive()) {
            s = "AVBL";
        } else if (isController()) {
            s = "CTRL";
        } else {
            s = "RESP";
        }
        return s;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RecordType other = (RecordType) obj;
        return flags == other.flags;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + flags;
        return result;
    }

    /**
     * Factory method for creating a RecordType from record flags as inactive
     *
     * @param flags the record flags to use
     * @return the inactive record type
     */
    public static RecordType asInactive(int flags) {
        return new RecordType(BinaryUtils.clearBit(flags, BIT_ACTIVE));
    }
}
