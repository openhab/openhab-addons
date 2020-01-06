/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

package org.openhab.binding.plugwiseha.internal.api.model.object;

import java.util.Map;
import java.util.Optional;

/**
 * The {@link Logs} class is an object model class that
 * mirrors the XML structure provided by the Plugwise Home Automation
 * controller for the collection of logs.
 * It extends the {@link PlugwiseHACollection} class.
 * 
 * @author B. van Wetten - Initial contribution
 */
public class Logs extends PlugwiseHACollection<Log> {

    private final String THERMOSTAT = "thermostat";
    private final String TEMPERATURE = "temperature";
    private final String BATTERY = "battery";
    private final String POWER_USAGE = "electricity_consumed";
    private final String RELAY = "relay";

    public Optional<Double> getTemperature() {
        return this.getLogTemperature().map(logEntry -> logEntry.getMeasurementAsDouble()).orElse(Optional.empty());
    }

    public Optional<Double> getThermostatTemperature() {
        return this.getLogThermostat().map(logEntry -> logEntry.getMeasurementAsDouble()).orElse(Optional.empty());
    }

    public Optional<String> getRelayState() {
        return this.getLogRelay().map(logEntry -> logEntry.getMeasurement()).orElse(Optional.empty());
    }

    public Optional<Double> getBatteryLevel() {
        return this.getLogBattery().map(logEntry -> logEntry.getMeasurementAsDouble()).orElse(Optional.empty());
    }

    public Optional<Double> getPowerUsage() {
        return this.getLogPowerUsage().map(logEntry -> logEntry.getMeasurementAsDouble()).orElse(Optional.empty());
    }

    public Optional<Log> getLogThermostat() {
        return Optional.ofNullable(this.get(THERMOSTAT));
    }

    public Optional<Log> getLogTemperature() {
        return Optional.ofNullable(this.get(TEMPERATURE));
    }

    public Optional<Log> getLogRelay() {
        return Optional.ofNullable(this.get(RELAY));
    }

    public Optional<Log> getLogBattery() {
        return Optional.ofNullable(this.get(BATTERY));
    }

    public Optional<Log> getLogPowerUsage() {
        return Optional.ofNullable(this.get(POWER_USAGE));
    }

    @Override
    public void merge(Map<String, Log> logs) {
        if (logs != null) {
            for (Log log : logs.values()) {
                String type = log.getType();
                Log updatedLog = this.get(type);

                try {
                    if (updatedLog == null || updatedLog.isOlderThan(log)) {
                        this.put(type, log);
                    }   
                } catch (NullPointerException e) {
                    e.toString();
                }          
            }
        }
    }
}