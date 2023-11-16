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

import java.util.ArrayList;

import javax.measure.quantity.Illuminance;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.touchwand.internal.dto.TouchWandAlarmSensorCurrentStatus.BinarySensorEvent;
import org.openhab.binding.touchwand.internal.dto.TouchWandAlarmSensorCurrentStatus.Sensor;
import org.openhab.binding.touchwand.internal.dto.TouchWandUnitData;
import org.openhab.binding.touchwand.internal.dto.TouchWandUnitDataAlarmSensor;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;

/**
 * The {@link TouchWandAlarmSensorHandler} is responsible for handling command for Alarm Sensor unit
 *
 *
 * @author Roie Geron - Initial contribution
 *
 */
@NonNullByDefault
public class TouchWandAlarmSensorHandler extends TouchWandBaseUnitHandler {

    private static final int BATT_LEVEL_LOW = 20;
    private static final int BATT_LEVEL_LOW_HYS = 5;

    private boolean isBatteryLow = false;
    private boolean isFirstUpdateTouchWandUnitState = true;

    public TouchWandAlarmSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    void updateTouchWandUnitState(TouchWandUnitData unitData) {
        if (unitData instanceof TouchWandUnitDataAlarmSensor sensor) {
            if (isFirstUpdateTouchWandUnitState) {
                removeUnsupportedChannels(sensor);
                isFirstUpdateTouchWandUnitState = false;
            }
            updateBatteryLevel(sensor);
            updateIllumination(sensor);
            updateChannelLeak(sensor);
            updateChannelDoorWindow(sensor);
            updateChannelMotion(sensor);
            updateChannelTemperature(sensor);
        } else {
            logger.warn("updateTouchWandUnitState incompatible TouchWandUnitData instance");
        }
    }

    @Override
    void touchWandUnitHandleCommand(Command command) {
    }

    void updateBatteryLevel(TouchWandUnitDataAlarmSensor unitData) {
        Integer battLevel = unitData.getCurrStatus().getBatt();
        updateState(CHANNEL_BATTERY_LEVEL, new DecimalType(battLevel));
        int lowThreshold = isBatteryLow ? BATT_LEVEL_LOW + BATT_LEVEL_LOW_HYS : BATT_LEVEL_LOW;
        boolean lowBattery = (battLevel <= lowThreshold);
        updateState(CHANNEL_BATTERY_LOW, OnOffType.from(lowBattery));
        isBatteryLow = lowBattery;
    }

    void updateIllumination(TouchWandUnitDataAlarmSensor unitData) {
        for (Sensor sensor : unitData.getCurrStatus().getSensorsStatus()) {
            if (sensor.type == SENSOR_TYPE_LUMINANCE) {
                updateState(CHANNEL_ILLUMINATION, new QuantityType<Illuminance>(sensor.value, Units.LUX));
            }
        }
    }

    void updateChannelLeak(TouchWandUnitDataAlarmSensor unitData) {
        for (BinarySensorEvent bSensor : unitData.getCurrStatus().getbSensorsStatus()) {
            if (bSensor.sensorType == SENSOR_TYPE_LEAK) {
                boolean isLeak = bSensor.sensor.state;
                updateState(CHANNEL_LEAK, OnOffType.from(isLeak));
            }
        }
    }

    void updateChannelDoorWindow(TouchWandUnitDataAlarmSensor unitData) {
        for (BinarySensorEvent bSensor : unitData.getCurrStatus().getbSensorsStatus()) {
            if (bSensor.sensorType == SENSOR_TYPE_DOOR_WINDOW) {
                boolean isOpen = bSensor.sensor.state;
                OpenClosedType myOpenClose;
                myOpenClose = isOpen ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                updateState(CHANNEL_DOORWINDOW, myOpenClose);
            }
        }
    }

    void updateChannelMotion(TouchWandUnitDataAlarmSensor unitData) {
        for (BinarySensorEvent bSensor : unitData.getCurrStatus().getbSensorsStatus()) {
            if (bSensor.sensorType == SENSOR_TYPE_MOTION) {
                boolean hasMotion = bSensor.sensor.state;
                updateState(CHANNEL_MOTION, OnOffType.from(hasMotion));
            }
        }
    }

    void updateChannelTemperature(TouchWandUnitDataAlarmSensor unitData) {
        for (Sensor sensor : unitData.getCurrStatus().getSensorsStatus()) {
            if (sensor.type == SENSOR_TYPE_TEMPERATURE) {
                updateState(CHANNEL_TEMPERATURE, new QuantityType<Temperature>(sensor.value, SIUnits.CELSIUS));
            }
        }
    }

    void removeUnsupportedChannels(TouchWandUnitDataAlarmSensor unitData) {
        ArrayList<Channel> toBeRemovedChannels = new ArrayList<>(thing.getChannels());

        for (BinarySensorEvent bSensor : unitData.getCurrStatus().getbSensorsStatus()) {
            switch (bSensor.sensorType) {
                case SENSOR_TYPE_MOTION:
                    toBeRemovedChannels.remove(thing.getChannel(CHANNEL_MOTION));
                    break;
                case SENSOR_TYPE_DOOR_WINDOW:
                    toBeRemovedChannels.remove(thing.getChannel(CHANNEL_DOORWINDOW));
                    break;
                case SENSOR_TYPE_LEAK:
                    toBeRemovedChannels.remove(thing.getChannel(CHANNEL_LEAK));
                    break;
            }
        }

        for (Sensor sensor : unitData.getCurrStatus().getSensorsStatus()) {
            switch (sensor.type) {
                case SENSOR_TYPE_TEMPERATURE:
                    toBeRemovedChannels.remove(thing.getChannel(CHANNEL_TEMPERATURE));
                    break;
                case SENSOR_TYPE_LUMINANCE:
                    toBeRemovedChannels.remove(thing.getChannel(CHANNEL_ILLUMINATION));
                    break;
            }
        }

        if (unitData.getHasBattery()) {
            toBeRemovedChannels.remove(thing.getChannel(CHANNEL_BATTERY_LEVEL));
            toBeRemovedChannels.remove(thing.getChannel(CHANNEL_BATTERY_LOW));
        }

        ThingBuilder thingBuilder = editThing();
        thingBuilder.withoutChannels(toBeRemovedChannels);
        updateThing(thingBuilder.build());
    }
}
