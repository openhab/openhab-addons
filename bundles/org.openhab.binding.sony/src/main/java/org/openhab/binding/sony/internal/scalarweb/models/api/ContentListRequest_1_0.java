/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents a content list request and is used for serialization only
 *
 * Versions:
 * <ol>
 * <li>1.0: {"source":"string", "stIdx":"int", "cnt":"int", "type":"string"}</li>
 * <li>1.1: unknown</li>
 * <li>1.2: {"source":"string", "stIdx":"int", "cnt":"int", "type":"string", "target":"string"}</li>
 * <li>1.3: unknown</li>
 * </ol>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ContentListRequest_1_0 {

    /** The source for the request */
    private final String source;

    /** The source starting index. */
    private final int stIdx;

    /** The count of items */
    private final int cnt;

    /**
     * Instantiates a new content list request.
     *
     * @param source the non-null, non-empty source
     * @param stIdx the starting index (>= 0)
     * @param cnt the total count (>= 0)
     */
    public ContentListRequest_1_0(final String source, final int stIdx, final int cnt) {
        Validate.notEmpty(source, "source cannot be empty");
        if (stIdx < -1) {
            throw new IllegalArgumentException("stIdx cannot be < 0: " + stIdx);
        }
        if (cnt < 0) {
            throw new IllegalArgumentException("cnt cannot be < 0: " + cnt);
        }

        this.source = source;
        this.stIdx = stIdx;
        this.cnt = cnt;
    }

    /**
     * Gets the source of the request
     *
     * @return the source of the request
     */
    public @Nullable String getSource() {
        return source;
    }

    /**
     * Gets the starting index
     *
     * @return the starting index
     */
    public int getStIdx() {
        return stIdx;
    }

    /**
     * Gets the total count
     *
     * @return the total count
     */
    public int getCnt() {
        return cnt;
    }

    @Override
    public String toString() {
        return "ContentListRequest_1_0 [source=" + source + ", stIdx=" + stIdx + ", cnt=" + cnt + "]";
    }
}
