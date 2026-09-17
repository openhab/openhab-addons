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
     * Get a human-readable description of this extraction strategy.
     */
    String describe();
}
