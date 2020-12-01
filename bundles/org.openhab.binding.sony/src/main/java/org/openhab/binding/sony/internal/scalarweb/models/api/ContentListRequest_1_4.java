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
 * <li>1.4: {"uri":"string", "stIdx":"int", "cnt":"int", "type":"string*", "target":"string", "view":"string",
 * "sort":"string", "path":"string"}</li>
 * <li>1.5: {"uri":"string", "stIdx":"int", "cnt":"int", "type":"string*", "target":"string", "view":"string",
 * "sort":"SortInfo", "search":"Search", "filter":"string*"}</li>
 * </ol>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ContentListRequest_1_4 {

    /** The uri for the request */
    private final String uri;

    /** The source starting index. */
    private final int stIdx;

    /** The count of items */
    private final int cnt;

    /**
     * Instantiates a new content list request.
     *
     * @param uri the non-null, non-empty uri
     * @param stIdx the starting index (>= 0)
     * @param cnt the total count (>= 0)
     */
    public ContentListRequest_1_4(final String uri, final int stIdx, final int cnt) {
        Validate.notEmpty(uri, "uri cannot be empty");
        if (stIdx < -1) {
            throw new IllegalArgumentException("stIdx cannot be < 0: " + stIdx);
        }
        if (cnt < 0) {
            throw new IllegalArgumentException("cnt cannot be < 0: " + cnt);
        }

        this.uri = uri;
        this.stIdx = stIdx;
        this.cnt = cnt;
    }

    /**
     * Gets the source of the request
     *
     * @return the source of the request
     */
    public @Nullable String getSource() {
        return uri;
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
        return "ContentListRequest_1_4 [uri=" + uri + ", stIdx=" + stIdx + ", cnt=" + cnt + "]";
    }
}
