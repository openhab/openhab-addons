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
package org.openhab.io.homekit.internal.accessories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hapjava.accessories.ThermostatAccessory;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.thermostat.CurrentHeatingCoolingStateEnum;
import io.github.hapjava.characteristics.impl.thermostat.TargetHeatingCoolingStateEnum;
import io.github.hapjava.characteristics.impl.thermostat.TemperatureDisplayUnitEnum;
import io.github.hapjava.services.impl.ThermostatService;

/**
 * Implements Thermostat as a GroupedAccessory made up of multiple items:
 * <ul>
 * <li>Current Temperature: Number type</li>
 * <li>Target Temperature: Number type</li>
 * <li>Current Heating/Cooling Mode: String type (see HomekitSettings.thermostat*Mode)</li>
 * <li>Target Heating/Cooling Mode: String type (see HomekitSettings.thermostat*Mode)</li>
 * </ul>
 *
 * @author Andy Lintner - Initial contribution
 */
class HomekitThermostatImpl extends AbstractHomekitAccessoryImpl implements ThermostatAccessory {
    private Logger LOGGER = LoggerFactory.getLogger(HomekitThermostatImpl.class);

    public HomekitThermostatImpl(HomekitTaggedItem taggedItem, List<HomekitTaggedItem> mandatoryCharacteristics,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws IncompleteAccessoryException {
        super(taggedItem, mandatoryCharacteristics, updater, settings);
        this.getServices().add(new ThermostatService(this));
    }

    @Override
    public CompletableFuture<CurrentHeatingCoolingStateEnum> getCurrentState() {
        String stringValue = getSettings().thermostatCurrentModeOff;
        final Optional<HomekitTaggedItem> characteristic = getCharacteristic(
                HomekitCharacteristicType.CURRENT_HEATING_COOLING_STATE);
        if (characteristic.isPresent()) {
            stringValue = characteristic.get().getItem().getState().toString();
        } else {
            LOGGER.error("Missing mandatory characteristic {}",
                    HomekitCharacteristicType.CURRENT_HEATING_COOLING_STATE);
        }

        CurrentHeatingCoolingStateEnum mode;

        if (stringValue.equalsIgnoreCase(getSettings().thermostatCurrentModeCooling)) {
            mode = CurrentHeatingCoolingStateEnum.COOL;
        } else if (stringValue.equalsIgnoreCase(getSettings().thermostatCurrentModeHeating)) {
            mode = CurrentHeatingCoolingStateEnum.HEAT;
        } else if (stringValue.equalsIgnoreCase(getSettings().thermostatCurrentModeOff)) {
            mode = CurrentHeatingCoolingStateEnum.OFF;
        } else if (stringValue.equals("UNDEF") || stringValue.equals("NULL")) {
            LOGGER.debug("Heating cooling target mode not available. Relaying value of OFF to Homekit");
            mode = CurrentHeatingCoolingStateEnum.OFF;
        } else {
            LOGGER.error("Unrecognized heatingCoolingCurrentMode: {}. Expected {}, {}, or {} strings in value.",
                    stringValue, getSettings().thermostatCurrentModeCooling, getSettings().thermostatCurrentModeHeating,
                    getSettings().thermostatCurrentModeOff);
            mode = CurrentHeatingCoolingStateEnum.OFF;
        }
        return CompletableFuture.completedFuture(mode);
    }

    @Override
    public CompletableFuture<Double> getCurrentTemperature() {
        DecimalType state = getStateAs(HomekitCharacteristicType.CURRENT_TEMPERATURE, DecimalType.class);
        return CompletableFuture.completedFuture(state != null ? convertToCelsius(state.doubleValue()) : 0.0);
    }

    @Override
    public CompletableFuture<TargetHeatingCoolingStateEnum> getTargetState() {
        String stringValue = getSettings().thermostatTargetModeOff;

        final Optional<HomekitTaggedItem> characteristic = getCharacteristic(
                HomekitCharacteristicType.TARGET_HEATING_COOLING_STATE);
        if (characteristic.isPresent()) {
            stringValue = characteristic.get().getItem().getState().toString();
        } else {
            LOGGER.error("Missing mandatory characteristic {}", HomekitCharacteristicType.TARGET_HEATING_COOLING_STATE);
        }
        TargetHeatingCoolingStateEnum mode;

        if (stringValue.equalsIgnoreCase(getSettings().thermostatTargetModeCool)) {
            mode = TargetHeatingCoolingStateEnum.COOL;
        } else if (stringValue.equalsIgnoreCase(getSettings().thermostatTargetModeHeat)) {
            mode = TargetHeatingCoolingStateEnum.HEAT;
        } else if (stringValue.equalsIgnoreCase(getSettings().thermostatTargetModeAuto)) {
            mode = TargetHeatingCoolingStateEnum.AUTO;
        } else if (stringValue.equalsIgnoreCase(getSettings().thermostatTargetModeOff)) {
            mode = TargetHeatingCoolingStateEnum.OFF;
        } else if (stringValue.equals("UNDEF") || stringValue.equals("NULL")) {
            LOGGER.debug("Heating cooling target mode not available. Relaying value of OFF to Homekit");
            mode = TargetHeatingCoolingStateEnum.OFF;
        } else {
            LOGGER.warn("Unrecognized heating cooling target mode: {}. Expected {}, {}, {}, or {} strings in value.",
                    stringValue, getSettings().thermostatTargetModeCool, getSettings().thermostatTargetModeHeat,
                    getSettings().thermostatTargetModeAuto, getSettings().thermostatTargetModeOff);
            mode = TargetHeatingCoolingStateEnum.OFF;
        }
        return CompletableFuture.completedFuture(mode);
    }

    @Override
    public CompletableFuture<TemperatureDisplayUnitEnum> getTemperatureDisplayUnit() {
        return CompletableFuture
                .completedFuture(getSettings().useFahrenheitTemperature ? TemperatureDisplayUnitEnum.FAHRENHEIT
                        : TemperatureDisplayUnitEnum.CELSIUS);
    }

    @Override
    public void setTemperatureDisplayUnit(final TemperatureDisplayUnitEnum value) throws Exception {
        // TODO: add support for display unit change
    }

    @Override
    public CompletableFuture<Double> getTargetTemperature() {
        DecimalType state = getStateAs(HomekitCharacteristicType.TARGET_TEMPERATURE, DecimalType.class);
        return CompletableFuture.completedFuture(state != null ? convertToCelsius(state.doubleValue()) : 0.0);
    }

    @Override
    public void setTargetState(TargetHeatingCoolingStateEnum mode) {
        String modeString = null;
        switch (mode) {
            case AUTO:
                modeString = getSettings().thermostatTargetModeAuto;
                break;

            case COOL:
                modeString = getSettings().thermostatTargetModeCool;
                break;

            case HEAT:
                modeString = getSettings().thermostatTargetModeHeat;
                break;

            case OFF:
                modeString = getSettings().thermostatTargetModeOff;
                break;
        }
        final Optional<HomekitTaggedItem> characteristic = getCharacteristic(
                HomekitCharacteristicType.TARGET_HEATING_COOLING_STATE);
        if (characteristic.isPresent()) {
            ((StringItem) characteristic.get().getItem()).send(new StringType(modeString));
        } else {
            LOGGER.error("Missing mandatory characteristic {}", HomekitCharacteristicType.TARGET_HEATING_COOLING_STATE);
        }
    }

    @Override
    public void setTargetTemperature(Double value) {
        final Optional<HomekitTaggedItem> characteristic = getCharacteristic(
                HomekitCharacteristicType.TARGET_TEMPERATURE);
        if (characteristic.isPresent()) {
            ((NumberItem) characteristic.get().getItem())
                    .send(new DecimalType(BigDecimal.valueOf(convertFromCelsius(value))));
        } else {
            LOGGER.error("Missing mandatory characteristic {}", HomekitCharacteristicType.TARGET_TEMPERATURE);
        }
    }

    @Override
    public void subscribeCurrentState(HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.CURRENT_HEATING_COOLING_STATE, callback);
    }

    @Override
    public void subscribeCurrentTemperature(HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.CURRENT_TEMPERATURE, callback);
    }

    @Override
    public void subscribeTargetState(HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.TARGET_HEATING_COOLING_STATE, callback);
    }

    @Override
    public void subscribeTargetTemperature(HomekitCharacteristicChangeCallback callback) {
        subscribe(HomekitCharacteristicType.TARGET_TEMPERATURE, callback);
    }

    @Override
    public void subscribeTemperatureDisplayUnit(final HomekitCharacteristicChangeCallback callback) {
        // TODO: add support for display unit change
    }

    @Override
    public void unsubscribeCurrentState() {
        unsubscribe(HomekitCharacteristicType.CURRENT_HEATING_COOLING_STATE);
    }

    @Override
    public void unsubscribeCurrentTemperature() {
        unsubscribe(HomekitCharacteristicType.CURRENT_TEMPERATURE);
    }

    @Override
    public void unsubscribeTemperatureDisplayUnit() {
        // TODO: add support for display unit change
    }

    @Override
    public void unsubscribeTargetState() {
        unsubscribe(HomekitCharacteristicType.TARGET_HEATING_COOLING_STATE);
    }

    @Override
    public void unsubscribeTargetTemperature() {
        unsubscribe(HomekitCharacteristicType.TARGET_TEMPERATURE);
    }

    protected double convertToCelsius(double degrees) {
        if (getSettings().useFahrenheitTemperature) {
            return Math.round((5d / 9d) * (degrees - 32d) * 1000d) / 1000d;
        } else {
            return degrees;
        }
    }

    protected double convertFromCelsius(double degrees) {
        if (getSettings().useFahrenheitTemperature) {
            return Math.round((((9d / 5d) * degrees) + 32d) * 10d) / 10d;
        } else {
            return degrees;
        }
    }
}
