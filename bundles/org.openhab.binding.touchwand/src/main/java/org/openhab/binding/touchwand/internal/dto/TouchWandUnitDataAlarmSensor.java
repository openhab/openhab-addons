/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.touchwand.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TouchWandUnitDataAlarmSensor} implements Alarm Sensor unit
 * data property.
 *
 * @author Roie Geron - Initial contribution
 */
@NonNullByDefault
public class TouchWandUnitDataAlarmSensor extends TouchWandUnitData {

    private TouchWandAlarmSensorCurrentStatus currStatus = new TouchWandAlarmSensorCurrentStatus();

    @Override
    public TouchWandAlarmSensorCurrentStatus getCurrStatus() {
        return this.currStatus;
    }

    public class AlarmEventType {
        public int eventsNum;
        public String description = "";
    }

    public class SensorType {
        public int type;
        public String description = "";
    }
}
