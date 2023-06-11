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
package org.openhab.binding.radiothermostat.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link RadioThermostatRuntimeDTO} is responsible for storing
 * the "today" and "yesterday" node from the "tstat/datalog" JSON response
 *
 * @author Michael Lobstein - Initial contribution
 */
public class RadioThermostatRuntimeDTO {

    @SerializedName("today")
    private RadioThermostatRuntimeHeatCoolDTO today;

    @SerializedName("yesterday")
    private RadioThermostatRuntimeHeatCoolDTO yesterday;

    public RadioThermostatRuntimeDTO() {
    }

    /**
     * Receives "today" node from the JSON response
     *
     * @return {RadioThermostatRuntimeHeatCool}
     */
    public RadioThermostatRuntimeHeatCoolDTO getToday() {
        return today;
    }

    /**
     * Receives "yesterday" node from the JSON response
     *
     * @return {RadioThermostatRuntimeHeatCool}
     */
    public RadioThermostatRuntimeHeatCoolDTO getYesterday() {
        return yesterday;
    }
}
