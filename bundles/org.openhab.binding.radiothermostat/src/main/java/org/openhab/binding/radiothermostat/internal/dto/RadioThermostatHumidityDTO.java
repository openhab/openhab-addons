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
 * The {@link RadioThermostatHumidityDTO} is responsible for storing
 * the data from the thermostat 'tstat/humidity' JSON response
 *
 * @author Michael Lobstein - Initial contribution
 */
public class RadioThermostatHumidityDTO {
    @SerializedName("humidity")
    private Integer humidity;

    public RadioThermostatHumidityDTO() {
    }

    public Integer getHumidity() {
        return humidity;
    }
}
