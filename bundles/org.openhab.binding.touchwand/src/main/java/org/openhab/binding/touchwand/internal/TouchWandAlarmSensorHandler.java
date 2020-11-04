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
package org.openhab.binding.touchwand.internal;

import static org.openhab.binding.touchwand.internal.TouchWandBindingConstants.*;

import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.touchwand.internal.dto.TouchWandAlarmSensorCurrentStatus.Sensor;
import org.openhab.binding.touchwand.internal.dto.TouchWandAlarmSensorCurrentStatus.bSensorEvent;
import org.openhab.binding.touchwand.internal.dto.TouchWandUnitData;
import org.openhab.binding.touchwand.internal.dto.TouchWandUnitDataAlarmSensor;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Thing;
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

    public TouchWandAlarmSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    void updateTouchWandUnitState(TouchWandUnitData unitData) {
        if (unitData instanceof TouchWandUnitDataAlarmSensor) {
            updateBatteryLevel((TouchWandUnitDataAlarmSensor) unitData);
            updateIllumination((TouchWandUnitDataAlarmSensor) unitData);
            updateChannelLeak((TouchWandUnitDataAlarmSensor) unitData);
            updateChannelDoorWindow((TouchWandUnitDataAlarmSensor) unitData);
            updateChannelMotion((TouchWandUnitDataAlarmSensor) unitData);
            updateChannelTemprature((TouchWandUnitDataAlarmSensor) unitData);
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
        isBatteryLow = (battLevel <= lowThreshold);
    }

    void updateIllumination(TouchWandUnitDataAlarmSensor unitData) {
        Iterator<Sensor> iter = unitData.getCurrStatus().getSensorsStatus().iterator();
        while (iter.hasNext()) {
            Sensor mySensor = iter.next();
            if (mySensor.type == SENSOR_TYPE_LUMINANCE) {
                updateState(CHANNEL_ILLUMINATION, new DecimalType(mySensor.value));
            }
        }
    }

    void updateChannelLeak(TouchWandUnitDataAlarmSensor unitData) {
        Iterator<bSensorEvent> iter = unitData.getCurrStatus().getbSensorsStatus().iterator();
        while (iter.hasNext()) {
            bSensorEvent mySensor = iter.next();
            if (mySensor.sensorType == SENSOR_TYPE_LEAK) {
                boolean isLeak = mySensor.sensor.state;
                updateState(CHANNEL_LEAK, OnOffType.from(isLeak));
            }
        }
    }

    void updateChannelDoorWindow(TouchWandUnitDataAlarmSensor unitData) {
        Iterator<bSensorEvent> iter = unitData.getCurrStatus().getbSensorsStatus().iterator();
        while (iter.hasNext()) {
            bSensorEvent mySensor = iter.next();
            if (mySensor.sensorType == SENSOR_TYPE_DOOR_WINDOW) {
                boolean isOpen = mySensor.sensor.state;
                OpenClosedType myOpenClose;
                myOpenClose = isOpen ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                updateState(CHANNEL_DOORWINDOW, myOpenClose);
            }
        }
    }

    void updateChannelMotion(TouchWandUnitDataAlarmSensor unitData) {
        Iterator<bSensorEvent> iter = unitData.getCurrStatus().getbSensorsStatus().iterator();
        while (iter.hasNext()) {
            bSensorEvent mySensor = iter.next();
            if (mySensor.sensorType == SENSOR_TYPE_MOTION) {
                boolean hasMotion = mySensor.sensor.state;
                updateState(CHANNEL_MOTION, OnOffType.from(hasMotion));
            }
        }
    }

    void updateChannelTemprature(TouchWandUnitDataAlarmSensor unitData) {
        Iterator<Sensor> iter = unitData.getCurrStatus().getSensorsStatus().iterator();
        while (iter.hasNext()) {
            Sensor mySensor = iter.next();
            if (mySensor.type == SENSOR_TYPE_TEMPERATURE) {
                updateState(CHANNEL_TEMPERATURE, new DecimalType(mySensor.value));
            }
        }
    }
}
