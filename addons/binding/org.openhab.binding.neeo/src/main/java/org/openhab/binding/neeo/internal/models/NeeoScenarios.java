/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.models;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
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
            if (StringUtils.equalsIgnoreCase(key, scenario.getKey())) {
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
