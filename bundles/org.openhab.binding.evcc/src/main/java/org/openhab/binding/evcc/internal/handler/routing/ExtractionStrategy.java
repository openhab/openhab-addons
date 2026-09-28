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
package org.openhab.binding.evcc.internal.handler.routing;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonElement;

/**
 * Strategy for extracting data from incoming websocket messages.
 * Implementations handle different data structures (arrays, maps, nested objects).
 */
@NonNullByDefault
public interface ExtractionStrategy {

    /**
     * Extract the relevant data element from the source.
     *
     * @param source The root element to extract from
     * @return The extracted data, or null if not found or invalid
     */
    @Nullable
    JsonElement extract(JsonElement source);

    /**
     * The immediate top-level property name this strategy extracts, if it can be determined
     * statically (e.g. {@code "solar"} for a {@code "$.solar"} JsonPath).
     * <p>
     * Used by {@link MessageRouter} to avoid redispatching sibling routes registered under
     * the same route key when an incoming delta is known to only affect one specific segment
     * (e.g. a {@code "forecast.solar"} update should not re-trigger the co2/feedin/grid
     * forecast routes with their unchanged, merged-cache data).
     *
     * @return the extracted property name, or {@code null} when it cannot be determined
     *         (in which case the route is always considered a candidate match)
     */
    @Nullable
    default String getTargetKey() {
        return null;
    }

    /**
     * Get a human-readable description of this extraction strategy.
     */
    String describe();
}
