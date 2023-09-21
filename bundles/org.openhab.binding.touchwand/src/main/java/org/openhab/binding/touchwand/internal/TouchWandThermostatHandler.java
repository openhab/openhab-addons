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
package org.openhab.binding.touchwand.internal;

import static org.openhab.binding.touchwand.internal.TouchWandBindingConstants.*;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.touchwand.internal.dto.TouchWandThermostatUnitData;
import org.openhab.binding.touchwand.internal.dto.TouchWandUnitData;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link TouchWandAlarmSensorHandler} is responsible for handling command for Alarm Sensor unit
 *
 * @author Roie Geron - Initial contribution
 */
@NonNullByDefault
public class TouchWandThermostatHandler extends TouchWandBaseUnitHandler {

    public TouchWandThermostatHandler(Thing thing) {
        super(thing);
    }

    @Override
    void updateTouchWandUnitState(TouchWandUnitData unitData) {
        if (unitData instanceof TouchWandThermostatUnitData thermostat) {
            updateThermostatState(thermostat);
            updateTargetTemperature(thermostat);
            updateRoomTemperature(thermostat);
            updateMode(thermostat);
            updateFanLevel(thermostat);
        } else {
            logger.warn("updateTouchWandUnitState incompatible TouchWandUnitData instance");
        }
    }

    @Override
    void touchWandUnitHandleCommand(Command command) {
        TouchWandBridgeHandler touchWandBridgeHandler = bridgeHandler;
        if (touchWandBridgeHandler != null) {
            if (command instanceof OnOffType onOffCommand) {
                touchWandBridgeHandler.touchWandClient.cmdThermostatOnOff(unitId, onOffCommand);
                return;
            }
            if (command instanceof QuantityType quantityCommand) {
                final QuantityType<?> value = quantityCommand.toUnit(SIUnits.CELSIUS);
                String targetTemperature = String.valueOf(value.intValue());
                touchWandBridgeHandler.touchWandClient.cmdThermostatTargetTemperature(unitId, targetTemperature);
                return;
            }

            String sCommand = command.toString();
            switch (sCommand) {
                case "cool":
                case "heat":
                case "fan":
                case "auto":
                case "dry":
                    touchWandBridgeHandler.touchWandClient.cmdThermostatMode(unitId, sCommand);
                    break;
                case "low":
                case "medium":
                case "high":
                    touchWandBridgeHandler.touchWandClient.cmdThermostatFanLevel(unitId, sCommand);
                    break;
                case "fanAuto":
                    touchWandBridgeHandler.touchWandClient.cmdThermostatFanLevel(unitId, "auto");
                    break;
                default:
                    break;
            }
        }
    }

    void updateThermostatState(TouchWandThermostatUnitData unitData) {
        String state = unitData.getCurrStatus().getState();
        updateState(CHANNEL_THERMOSTAT_STATE, OnOffType.from(state));
    }

    void updateTargetTemperature(TouchWandThermostatUnitData unitData) {
        int targetTemperature = unitData.getCurrStatus().getTargetTemperature();
        QuantityType<Temperature> temperatureValue = new QuantityType<Temperature>(targetTemperature, SIUnits.CELSIUS);
        updateState(CHANNEL_THERMOSTAT_TARGET_TEMPERATURE, temperatureValue);
    }

    void updateRoomTemperature(TouchWandThermostatUnitData unitData) {
        int roomTemperature = unitData.getCurrStatus().getRoomTemperature();
        QuantityType<Temperature> temperatureValue = new QuantityType<Temperature>(roomTemperature, SIUnits.CELSIUS);
        updateState(CHANNEL_THERMOSTAT_ROOM_TEMPERATURE, temperatureValue);
    }

    void updateMode(TouchWandThermostatUnitData unitData) {
        String mode = unitData.getCurrStatus().getMode();
        StringType newVal = StringType.valueOf(mode);
        updateState(CHANNEL_THERMOSTAT_MODE, newVal);
    }

    void updateFanLevel(TouchWandThermostatUnitData unitData) {
        String fanLevel = unitData.getCurrStatus().getFanLevel();
        StringType newVal = StringType.valueOf(fanLevel);
        updateState(CHANNEL_THERMOSTAT_FAN_LEVEL, newVal);
    }
}
