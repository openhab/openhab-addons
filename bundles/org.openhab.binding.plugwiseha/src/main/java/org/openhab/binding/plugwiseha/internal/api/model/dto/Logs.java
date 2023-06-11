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
package org.openhab.binding.plugwiseha.internal.api.model.dto;

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

    private static final String THERMOSTAT = "thermostat";
    private static final String TEMPERATURE = "temperature";
    private static final String TEMPERATURE_OFFSET = "temperature_offset";
    private static final String BATTERY = "battery";
    private static final String POWER_USAGE = "electricity_consumed";
    private static final String RELAY = "relay";
    private static final String DHWSTATE = "domestic_hot_water_state";
    private static final String COOLINGSTATE = "cooling_state";
    private static final String INTENDEDBOILERTEMP = "intended_boiler_temperature";
    private static final String FLAMESTATE = "flame_state";
    private static final String INTENDEDHEATINGSTATE = "intended_central_heating_state";
    private static final String MODULATIONLEVEL = "modulation_level";
    private static final String OTAPPLICATIONFAULTCODE = "open_therm_application_specific_fault_code";
    private static final String DHWTEMP = "domestic_hot_water_temperature";
    private static final String OTOEMFAULTCODE = "open_therm_oem_fault_code";
    private static final String BOILERTEMP = "boiler_temperature";
    private static final String DHWSETPOINT = "domestic_hot_water_setpoint";
    private static final String MAXBOILERTEMP = "maximum_boiler_temperature";
    private static final String DHWCOMFORTMODE = "domestic_hot_water_comfort_mode";
    private static final String CHSTATE = "central_heating_state";
    private static final String VALVE_POSITION = "valve_position";
    private static final String WATER_PRESSURE = "central_heater_water_pressure";

    public Optional<Boolean> getCoolingState() {
        return this.getLog(COOLINGSTATE).map(logEntry -> logEntry.getMeasurementAsBoolean()).orElse(Optional.empty());
    }

    public Optional<Double> getIntendedBoilerTemp() {
        return this.getLog(INTENDEDBOILERTEMP).map(logEntry -> logEntry.getMeasurementAsDouble())
                .orElse(Optional.empty());
    }

    public Optional<String> getIntendedBoilerTempUnit() {
        return this.getLog(INTENDEDBOILERTEMP).map(logEntry -> logEntry.getMeasurementUnit()).orElse(Optional.empty());
    }

    public Optional<Boolean> getFlameState() {
        return this.getLog(FLAMESTATE).map(logEntry -> logEntry.getMeasurementAsBoolean()).orElse(Optional.empty());
    }

    public Optional<Boolean> getIntendedHeatingState() {
        return this.getLog(INTENDEDHEATINGSTATE).map(logEntry -> logEntry.getMeasurementAsBoolean())
                .orElse(Optional.empty());
    }

    public Optional<Double> getModulationLevel() {
        return this.getLog(MODULATIONLEVEL).map(logEntry -> logEntry.getMeasurementAsDouble()).orElse(Optional.empty());
    }

    public Optional<Double> getOTAppFaultCode() {
        return this.getLog(OTAPPLICATIONFAULTCODE).map(logEntry -> logEntry.getMeasurementAsDouble())
                .orElse(Optional.empty());
    }

    public Optional<Double> getDHWTemp() {
        return this.getLog(DHWTEMP).map(logEntry -> logEntry.getMeasurementAsDouble()).orElse(Optional.empty());
    }

    public Optional<String> getDHWTempUnit() {
        return this.getLog(DHWTEMP).map(logEntry -> logEntry.getMeasurementUnit()).orElse(Optional.empty());
    }

    public Optional<Double> getOTOEMFaultcode() {
        return this.getLog(OTOEMFAULTCODE).map(logEntry -> logEntry.getMeasurementAsDouble()).orElse(Optional.empty());
    }

    public Optional<Double> getBoilerTemp() {
        return this.getLog(BOILERTEMP).map(logEntry -> logEntry.getMeasurementAsDouble()).orElse(Optional.empty());
    }

    public Optional<String> getBoilerTempUnit() {
        return this.getLog(BOILERTEMP).map(logEntry -> logEntry.getMeasurementUnit()).orElse(Optional.empty());
    }

    public Optional<Double> getDHTSetpoint() {
        return this.getLog(DHWSETPOINT).map(logEntry -> logEntry.getMeasurementAsDouble()).orElse(Optional.empty());
    }

    public Optional<String> getDHTSetpointUnit() {
        return this.getLog(DHWSETPOINT).map(logEntry -> logEntry.getMeasurementUnit()).orElse(Optional.empty());
    }

    public Optional<Double> getMaxBoilerTemp() {
        return this.getLog(MAXBOILERTEMP).map(logEntry -> logEntry.getMeasurementAsDouble()).orElse(Optional.empty());
    }

    public Optional<String> getMaxBoilerTempUnit() {
        return this.getLog(MAXBOILERTEMP).map(logEntry -> logEntry.getMeasurementUnit()).orElse(Optional.empty());
    }

    public Optional<Boolean> getDHWComfortMode() {
        return this.getLog(DHWCOMFORTMODE).map(logEntry -> logEntry.getMeasurementAsBoolean()).orElse(Optional.empty());
    }

    public Optional<Double> getTemperature() {
        return this.getLog(TEMPERATURE).map(logEntry -> logEntry.getMeasurementAsDouble()).orElse(Optional.empty());
    }

    public Optional<String> getTemperatureUnit() {
        return this.getLog(TEMPERATURE).map(logEntry -> logEntry.getMeasurementUnit()).orElse(Optional.empty());
    }

    public Optional<Double> getThermostatTemperature() {
        return this.getLog(THERMOSTAT).map(logEntry -> logEntry.getMeasurementAsDouble()).orElse(Optional.empty());
    }

    public Optional<String> getThermostatTemperatureUnit() {
        return this.getLog(THERMOSTAT).map(logEntry -> logEntry.getMeasurementUnit()).orElse(Optional.empty());
    }

    public Optional<Double> getOffsetTemperature() {
        return this.getLog(TEMPERATURE_OFFSET).map(logEntry -> logEntry.getMeasurementAsDouble())
                .orElse(Optional.empty());
    }

    public Optional<String> getOffsetTemperatureUnit() {
        return this.getLog(TEMPERATURE_OFFSET).map(logEntry -> logEntry.getMeasurementUnit()).orElse(Optional.empty());
    }

    public Optional<Boolean> getRelayState() {
        return this.getLog(RELAY).map(logEntry -> logEntry.getMeasurementAsBoolean()).orElse(Optional.empty());
    }

    public Optional<Boolean> getDHWState() {
        return this.getLog(DHWSTATE).map(logEntry -> logEntry.getMeasurementAsBoolean()).orElse(Optional.empty());
    }

    public Optional<Boolean> getCHState() {
        return this.getLog(CHSTATE).map(logEntry -> logEntry.getMeasurementAsBoolean()).orElse(Optional.empty());
    }

    public Optional<Double> getValvePosition() {
        return this.getLog(VALVE_POSITION).map(logEntry -> logEntry.getMeasurementAsDouble()).orElse(Optional.empty());
    }

    public Optional<Double> getWaterPressure() {
        return this.getLog(WATER_PRESSURE).map(logEntry -> logEntry.getMeasurementAsDouble()).orElse(Optional.empty());
    }

    public Optional<Double> getBatteryLevel() {
        return this.getLog(BATTERY).map(logEntry -> logEntry.getMeasurementAsDouble()).orElse(Optional.empty());
    }

    public Optional<Double> getPowerUsage() {
        return this.getLog(POWER_USAGE).map(logEntry -> logEntry.getMeasurementAsDouble()).orElse(Optional.empty());
    }

    public Optional<Log> getLog(String logItem) {
        return Optional.ofNullable(this.get(logItem));
    }

    @Override
    public void merge(Map<String, Log> logsToMerge) {
        if (logsToMerge != null) {
            for (Log logToMerge : logsToMerge.values()) {
                String type = logToMerge.getType();
                Log originalLog = this.get(type);

                if (originalLog == null || originalLog.isOlderThan(logToMerge)) {
                    this.put(type, logToMerge);
                } else {
                    this.put(type, originalLog);
                }
            }
        }
    }
}
