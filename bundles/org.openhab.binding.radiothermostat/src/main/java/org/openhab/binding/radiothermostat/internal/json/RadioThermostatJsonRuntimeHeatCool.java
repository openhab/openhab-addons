/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.radiothermostat.internal.json;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link RadioThermostatJsonRuntimeHeatCool} is responsible for storing
 * the "heat_runtime" and "cool_runtime" node from the thermostat JSON response
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class RadioThermostatJsonRuntimeHeatCool {

    public RadioThermostatJsonRuntimeHeatCool() {
    }

    @SerializedName("heat_runtime")
    private @Nullable RadioThermostatJsonTime heatTime;

    @SerializedName("cool_runtime")
    private @Nullable RadioThermostatJsonTime coolTime;

    /**
     * Receives "heat_runtime" node from the JSON response
     *
     * @return {RadioThermostatJsonTime}
     */
    public @Nullable RadioThermostatJsonTime getHeatTime() {
        return heatTime;
    }

    /**
     * Receives "cool_runtime" node from the JSON response
     *
     * @return {RadioThermostatJsonTime}
     */
    public @Nullable RadioThermostatJsonTime getCoolTime() {
        return coolTime;
    }
}
