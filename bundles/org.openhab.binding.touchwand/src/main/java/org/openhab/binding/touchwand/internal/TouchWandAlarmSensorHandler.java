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
package org.openhab.binding.touchwand.internal;

import static org.openhab.binding.touchwand.internal.TouchWandBindingConstants.*;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.touchwand.internal.data.TouchWandUnitData;
import org.openhab.binding.touchwand.internal.data.TouchWandUnitDataAlarmSensor;

/**
 * The {@link TouchWandAlarmSensorHandler} is responsible for handling Alarm Sensor triggers
 *
 * for WallController units
 *
 * @author Roie Geron - Initial contribution
 *
 */
@NonNullByDefault
public class TouchWandAlarmSensorHandler extends TouchWandBaseUnitHandler {

    public TouchWandAlarmSensorHandler(Thing thing) {
        super(thing);
        Instant.now().toEpochMilli();
    }

    @Override
    void touhWandUnitHandleCommand(Command command) {
    }

    @Override
    void updateTouchWandUnitState(TouchWandUnitData unitData) {
        Integer batteryLevel = ((TouchWandUnitDataAlarmSensor) unitData).getCurrStatus().getBatt();
        DecimalType dBatteryLevel = DecimalType.valueOf(String.valueOf(batteryLevel));
        updateState(CHANNEL_BATTERY_LEVEL, dBatteryLevel);
        updateState(CHANNEL_BATTERY_LOW, dBatteryLevel.intValue() <= 10 ? OnOffType.ON : OnOffType.OFF);
    }
}
