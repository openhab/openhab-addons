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

package org.openhab.binding.plugwiseha.internal.api.model.DTO;

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
    private final String TEMPERATURE_OFFSET = "temperature_offset";
    private final String BATTERY = "battery";
    private final String POWER_USAGE = "electricity_consumed";
    private final String RELAY = "relay";
    private final String DHWSTATE = "domestic_hot_water_state";
    private final String CHSTATE = "central_heating_state";
    private final String VALVE_POSITION = "valve_position";
    private final String WATER_PRESSURE = "central_heater_water_pressure";

    public Optional<Double> getTemperature() {
        return this.getLogTemperature().map(logEntry -> logEntry.getMeasurementAsDouble()).orElse(Optional.empty());
    }

    public Optional<Double> getThermostatTemperature() {
        return this.getLogThermostat().map(logEntry -> logEntry.getMeasurementAsDouble()).orElse(Optional.empty());
    }

    public Optional<Double> getOffsetTemperature() {
        return this.getLogOffsetTemperature().map(logEntry -> logEntry.getMeasurementAsDouble())
                .orElse(Optional.empty());
    }

    public Optional<Boolean> getRelayState() {
        return this.getLogRelay().map(logEntry -> logEntry.getMeasurementAsBoolean()).orElse(Optional.empty());
    }

    public Optional<Boolean> getDHWState() {
        return this.getLogDHWState().map(logEntry -> logEntry.getMeasurementAsBoolean()).orElse(Optional.empty());
    }

    public Optional<Boolean> getCHState() {
        return this.getLogCHState().map(logEntry -> logEntry.getMeasurementAsBoolean()).orElse(Optional.empty());
    }

    public Optional<Double> getValvePosition() {
        return this.getLogValvePosition().map(logEntry -> logEntry.getMeasurementAsDouble()).orElse(Optional.empty());
    }

    public Optional<Double> getWaterPressure() {
        return this.getLogPressure().map(logEntry -> logEntry.getMeasurementAsDouble()).orElse(Optional.empty());
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

    public Optional<Log> getLogOffsetTemperature() {
        return Optional.ofNullable(this.get(TEMPERATURE_OFFSET));
    }

    public Optional<Log> getLogTemperature() {
        return Optional.ofNullable(this.get(TEMPERATURE));
    }

    public Optional<Log> getLogRelay() {
        return Optional.ofNullable(this.get(RELAY));
    }

    public Optional<Log> getLogDHWState() {
        return Optional.ofNullable(this.get(DHWSTATE));
    }

    public Optional<Log> getLogCHState() {
        return Optional.ofNullable(this.get(CHSTATE));
    }

    public Optional<Log> getLogValvePosition() {
        return Optional.ofNullable(this.get(VALVE_POSITION));
    }

    public Optional<Log> getLogPressure() {
        return Optional.ofNullable(this.get(WATER_PRESSURE));
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
