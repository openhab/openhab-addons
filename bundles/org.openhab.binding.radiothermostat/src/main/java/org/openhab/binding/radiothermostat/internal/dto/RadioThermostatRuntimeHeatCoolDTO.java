/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.radiothermostat.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link RadioThermostatRuntimeHeatCoolDTO} is responsible for storing
 * the "heat_runtime" and "cool_runtime" node from the thermostat JSON response
 *
 * @author Michael Lobstein - Initial contribution
 */
public class RadioThermostatRuntimeHeatCoolDTO {

    public RadioThermostatRuntimeHeatCoolDTO() {
    }

    @SerializedName("heat_runtime")
    private RadioThermostatTimeDTO heatTime;

    @SerializedName("cool_runtime")
    private RadioThermostatTimeDTO coolTime;

    /**
     * Receives "heat_runtime" node from the JSON response
     *
     * @return {RadioThermostatJsonTime}
     */
    public RadioThermostatTimeDTO getHeatTime() {
        return heatTime;
    }

    /**
     * Receives "cool_runtime" node from the JSON response
     *
     * @return {RadioThermostatJsonTime}
     */
    public RadioThermostatTimeDTO getCoolTime() {
        return coolTime;
    }
}
