/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.io.mcp.internal.tools;

import static org.openhab.io.mcp.internal.tools.McpToolUtils.getIntArg;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Paging shared by the list tools ({@code search_items}, {@code get_things}, {@code get_rules}).
 * Out-of-range {@code limit} and {@code offset} arguments are clamped rather than rejected, so a
 * call always returns a page.
 *
 * @param maxLimit upper bound for {@code limit}, from the {@code maxItemsPerPage} setting
 * @param defaultLimit {@code limit} used when the caller passes none
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
record Paging(int maxLimit, int defaultLimit) {

    static final int DEFAULT_PAGE_LIMIT = 50;

    /**
     * Creates the paging rules for a configured {@code maxItemsPerPage}; values below 1 are treated as 1.
     */
    static Paging of(int maxItemsPerPage) {
        int max = Math.max(1, maxItemsPerPage);
        return new Paging(max, Math.min(DEFAULT_PAGE_LIMIT, max));
    }

    /**
     * Adds the {@code limit} and {@code offset} input-schema properties. The cap is stated in the
     * description rather than as a JSON-schema {@code maximum}, so strict clients still send larger
     * values and the server clamps them.
     */
    void addSchemaProperties(Map<String, Object> properties) {
        properties.put("limit",
                Map.of("type", "integer", "minimum", 1, "description", "Maximum results to return" + " (default: "
                        + defaultLimit + ", max: " + maxLimit + "; larger values are capped to the max)."
                        + " To get more, repeat the call with offset increased by the number of results returned"
                        + " until offset reaches total."));
        properties.put("offset",
                Map.of("type", "integer", "minimum", 0, "description", "Number of results to skip (default: 0)"));
    }

    /**
     * Returns the page of {@code all} selected by the {@code limit} and {@code offset} arguments and
     * records the applied {@code total}, {@code offset} and {@code limit} in {@code response}.
     */
    <T> List<T> page(List<T> all, Map<String, Object> args, Map<String, Object> response) {
        int limit = Math.max(1, Math.min(maxLimit, getIntArg(args, "limit", defaultLimit)));
        int offset = Math.max(0, getIntArg(args, "offset", 0));
        int total = all.size();
        int start = Math.min(offset, total);
        response.put("total", total);
        response.put("offset", offset);
        response.put("limit", limit);
        return all.subList(start, Math.min(start + limit, total));
    }
}
