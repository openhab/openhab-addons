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
package org.openhab.binding.atv.internal.client.dto;

import java.util.Map;

/**
 * Feature state and options.
 *
 * @param state current state of the feature
 * @param options feature specific options (never {@code null})
 *
 * @author Dan Cunningham - Initial contribution
 */
public record FeatureInfo(FeatureState state, Map<String, Object> options) {

    /**
     * Canonical constructor normalizing {@code options} to an immutable, non-null map.
     */
    public FeatureInfo {
        options = options == null ? Map.of() : Map.copyOf(options);
    }

    /**
     * Creates feature info without options.
     *
     * @param state current state of the feature
     */
    public FeatureInfo(FeatureState state) {
        this(state, Map.of());
    }
}
