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

/**
 * The {@link RadioThermostatData} is responsible for storing
 * all of the JSON data objects that are retrieved from the thermostat
 *
 * @author Michael Lobstein - Initial contribution
 */
public class RadioThermostatData {    
    private String name;
    private String model;
    private RadioThermostatJsonResponse thermostatData;
    private Integer humidity;
    private RadioThermostatJsonRuntime runtime;
    
    public RadioThermostatData() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public RadioThermostatJsonResponse getThermostatData() {
        return thermostatData;
    }

    public void setThermostatData(RadioThermostatJsonResponse thermostatData) {
        this.thermostatData = thermostatData;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    public RadioThermostatJsonRuntime getRuntime() {
        return runtime;
    }

    public void setRuntime(RadioThermostatJsonRuntime runtime) {
        this.runtime = runtime;
    }
    
}
