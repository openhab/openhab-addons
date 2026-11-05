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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Health information from the mower. This information is just included if the robonect module runs the firmware
 * 1.0 beta or higher.
 * 
 * @author Marco Meyer - Initial contribution
 */
public class Health {

    private @Nullable Integer temperature;

    private @Nullable Integer humidity;

    /**
     * Returns the temperature in °C measured in the mower.
     *
     * @return the temperature in °C measured in the mower.
     */
    public @Nullable Integer getTemperature() {
        return temperature;
    }

    /**
     * Returns the humidity in % measured in the mower.
     *
     * @return the humidity in % measured in the mower.
     */
    public @Nullable Integer getHumidity() {
        return humidity;
    }
}
