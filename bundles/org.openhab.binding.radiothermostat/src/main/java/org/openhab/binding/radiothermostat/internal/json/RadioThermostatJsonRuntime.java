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
 * The {@link RadioThermostatJsonRuntime} is responsible for storing
 * the "today" and "yesterday" node from the "tstat/datalog" JSON response
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class RadioThermostatJsonRuntime {

    @SerializedName("today")
    private @Nullable RadioThermostatJsonRuntimeHeatCool today;

    @SerializedName("yesterday")
    private @Nullable RadioThermostatJsonRuntimeHeatCool yesterday;

    public RadioThermostatJsonRuntime() {
    }

    /**
     * Receives "today" node from the JSON response
     *
     * @return {RadioThermostatRuntimeHeatCool}
     */
    public @Nullable RadioThermostatJsonRuntimeHeatCool getToday() {
        return today;
    }

    /**
     * Receives "yesterday" node from the JSON response
     *
     * @return {RadioThermostatRuntimeHeatCool}
     */
    public @Nullable RadioThermostatJsonRuntimeHeatCool getYesterday() {
        return yesterday;
    }
}
