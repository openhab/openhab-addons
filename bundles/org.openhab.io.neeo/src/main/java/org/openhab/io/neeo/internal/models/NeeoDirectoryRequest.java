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
package org.openhab.io.neeo.internal.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents a directory position (within an overall list of items). This class is simply used for
 * serialization and deserialization with the NEEO Brain.
 *
 * @author Tim Roberts - Initial Contribution
 *
 */
@NonNullByDefault
public class NeeoDirectoryRequest {
    /** The offset position within the overall list */
    private final int offset;

    /** The limit (total items) for this position */
    private final int limit;

    /** The browse identifier identifying this position */
    @Nullable
    private final String browseIdentifier;

    /**
     * Constructs the position from the given parameters
     *
     * @param offset a non-negative offset
     * @param limit a non-negative limit
     * @param browseIdentifier a potentially null, potentially empty browse identifier
     */
    public NeeoDirectoryRequest(int offset, int limit, @Nullable String browseIdentifier) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset cannot be negative");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("limit cannot be negative");
        }
        this.offset = offset;
        this.limit = limit;
        this.browseIdentifier = browseIdentifier;
    }

    /**
     * The offset for this position
     *
     * @return the offset (>= 0)
     */
    public int getOffset() {
        return offset;
    }

    /**
     * The limit for this position
     *
     * @return the limit (>= 0)
     */
    public int getLimit() {
        return limit;
    }

    /**
     * The browse identifier
     *
     * @return a potentially null, potentially empty browse identifier
     */
    @Nullable
    public String getBrowseIdentifier() {
        return browseIdentifier;
    }

    @Override
    public String toString() {
        return "NeeoDiscoveryListResultPosition [offset=" + offset + ", limit=" + limit + ", browseIdentifier="
                + browseIdentifier + "]";
    }
}
