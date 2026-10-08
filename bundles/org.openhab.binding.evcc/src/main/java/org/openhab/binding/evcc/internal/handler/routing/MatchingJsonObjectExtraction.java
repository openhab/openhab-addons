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
 * Extraction strategy that accepts an already normalized JSON object.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class MatchingJsonObjectExtraction implements ExtractionStrategy {

    private final String requiredKey;

    public MatchingJsonObjectExtraction(String requiredKey) {
        this.requiredKey = requiredKey;
    }

    @Override
    public @Nullable JsonElement extract(JsonElement source) {
        return source.isJsonObject() && source.getAsJsonObject().has(requiredKey) ? source : null;
    }

    @Override
    public String describe() {
        return "MatchingObject{" + requiredKey + "}";
    }
}
