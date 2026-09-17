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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonElement;

/**
 * Extraction strategy that tries several strategies and returns the first successful result.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class FallbackExtraction implements ExtractionStrategy {

    private final List<ExtractionStrategy> strategies;

    public FallbackExtraction(ExtractionStrategy... strategies) {
        this.strategies = List.of(strategies);
    }

    @Override
    public @Nullable JsonElement extract(JsonElement source) {
        for (ExtractionStrategy strategy : strategies) {
            JsonElement extracted = strategy.extract(source);
            if (extracted != null) {
                return extracted;
            }
        }
        return null;
    }

    @Override
    public String describe() {
        return "Fallback["
                + strategies.stream().map(ExtractionStrategy::describe).reduce((a, b) -> a + ", " + b).orElse("") + "]";
    }
}
