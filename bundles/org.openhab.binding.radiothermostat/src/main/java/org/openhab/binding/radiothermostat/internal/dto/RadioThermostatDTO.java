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

/**
 * The {@link RadioThermostatDTO} is responsible for storing
 * all of the JSON data objects that are retrieved from the thermostat
 *
 * @author Michael Lobstein - Initial contribution
 */
public class RadioThermostatDTO {
    private RadioThermostatTstatDTO thermostatData;
    private Integer humidity;
    private RadioThermostatRuntimeDTO runtime;

    public RadioThermostatDTO() {
    }

    public RadioThermostatTstatDTO getThermostatData() {
        return thermostatData;
    }

    public void setThermostatData(RadioThermostatTstatDTO thermostatData) {
        this.thermostatData = thermostatData;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    public RadioThermostatRuntimeDTO getRuntime() {
        return runtime;
    }

    public void setRuntime(RadioThermostatRuntimeDTO runtime) {
        this.runtime = runtime;
    }
}
