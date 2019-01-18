/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.neeo.internal.models;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents the meta data information to a directly result. This class is simply used for serialization of the result
 * back to the brain.
 *
 * @author Tim Roberts - Initial Contribution
 *
 */
@NonNullByDefault
public class NeeoDirectoryResultMeta {
    /** The total items available */
    private final int totalItems;

    /** The total matching items (if filtered) available */
    private final int totalMatchingItems;

    /** The current position request (that will be sent back to us) */
    private final NeeoDirectoryRequest current;

    /** The previous position that will be sent back to us (null if no previous) */
    @Nullable
    private final NeeoDirectoryRequest previous;

    /** The next position that will be sent back to us (null if no next) */
    @Nullable
    private final NeeoDirectoryRequest next;

    /**
     * Constructs the meta data from the given items
     *
     * @param totalItems the total items available (>= 0)
     * @param totalMatchingItems the total matching items available (>= 0)
     * @param current the non-null current position
     * @param previous the possibly null previous position
     * @param next the possibly null next position
     */
    public NeeoDirectoryResultMeta(int totalItems, int totalMatchingItems, NeeoDirectoryRequest current,
            @Nullable NeeoDirectoryRequest previous, @Nullable NeeoDirectoryRequest next) {
        Objects.requireNonNull(current, "current cannot be null");
        if (totalItems < 0) {
            throw new IllegalArgumentException("totalItems must be >= 0");
        }
        if (totalMatchingItems < 0) {
            throw new IllegalArgumentException("totalMatchingItems must be >= 0");
        }

        this.totalItems = totalItems;
        this.totalMatchingItems = totalMatchingItems;
        this.current = current;
        this.previous = previous;
        this.next = next;
    }

    /**
     * Returns the total items available
     *
     * @return the total items (>= 0)
     */
    public int getTotalItems() {
        return totalItems;
    }

    /**
     * Returns the total matching items available
     *
     * @return the total matching items (>= 0)
     */
    public int getTotalMatchingItems() {
        return totalMatchingItems;
    }

    /**
     * Returns the current position
     *
     * @return a non-null current position
     */
    public NeeoDirectoryRequest getCurrent() {
        return current;
    }

    /**
     * Returns the previous position
     *
     * @return a possibly null previous position (null if none)
     */
    @Nullable
    public NeeoDirectoryRequest getPrevious() {
        return previous;
    }

    /**
     * Returns the next position
     *
     * @return a possibly null next position (null if none)
     */
    @Nullable
    public NeeoDirectoryRequest getNext() {
        return next;
    }

    @Override
    public String toString() {
        return "NeeoDiscoveryListResultMeta [totalItems=" + totalItems + ", totalMatchingItems=" + totalMatchingItems
                + ", current=" + current + ", previous=" + previous + ", next=" + next + "]";
    }

}