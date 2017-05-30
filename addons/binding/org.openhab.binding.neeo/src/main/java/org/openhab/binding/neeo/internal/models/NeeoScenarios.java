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

import org.apache.commons.lang.StringUtils;

/**
 * The model representing Neeo Scenarios (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial contribution
 */
public class NeeoScenarios {

    /** The scenarios. */
    private final NeeoScenario[] scenarios;

    /**
     * Instantiates a new neeo scenarios.
     *
     * @param scenarios the scenarios
     */
    public NeeoScenarios(NeeoScenario[] scenarios) {
        this.scenarios = scenarios;
    }

    /**
     * Gets the scenarios.
     *
     * @return the scenarios
     */
    public NeeoScenario[] getScenarios() {
        return scenarios;
    }

    /**
     * Gets the scenario.
     *
     * @param key the key
     * @return the scenario
     */
    public NeeoScenario getScenario(String key) {
        for (NeeoScenario scenario : scenarios) {
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
