/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.neeo.internal.models;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The model representing Neeo Scenarios (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NeeoScenarios {

    /** The scenarios. */
    private final NeeoScenario @Nullable [] scenarios;

    /**
     * Instantiates a new neeo scenarios.
     *
     * @param scenarios the scenarios
     */
    NeeoScenarios(NeeoScenario[] scenarios) {
        Objects.requireNonNull(scenarios, "scenarios cannot be null");
        this.scenarios = scenarios;
    }

    /**
     * Gets the scenarios.
     *
     * @return the scenarios
     */
    public NeeoScenario[] getScenarios() {
        final NeeoScenario @Nullable [] localScenarios = scenarios;
        return localScenarios == null ? new NeeoScenario[0] : localScenarios;
    }

    /**
     * Gets the scenario.
     *
     * @param key the key
     * @return the scenario
     */
    @Nullable
    public NeeoScenario getScenario(String key) {
        for (NeeoScenario scenario : getScenarios()) {
            if (key.equalsIgnoreCase(scenario.getKey())) {
                return scenario;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "NeeoScenarios [scenarios=" + Arrays.toString(scenarios) + "]";
    }
}
