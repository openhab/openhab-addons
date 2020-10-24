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

package org.openhab.binding.touchwand.internal.dto;

/**
 * The {@link TouchWandUnitDataAlarmSensor} implements Alarm Sensor unit
 * data property.
 *
 * @author Roie Geron - Initial contribution
 */
public class TouchWandUnitDataAlarmSensor extends TouchWandUnitData {

    public TouchWandAlarmSensorCurrentStatus currStatus = new TouchWandAlarmSensorCurrentStatus();

    public void setCurrStatus(TouchWandAlarmSensorCurrentStatus currStatus) {
        this.currStatus = currStatus;
    }

    @Override
    public TouchWandAlarmSensorCurrentStatus getCurrStatus() {
        return this.currStatus;
    }

    // public List<Sensor> getSensors() {
    // return sensors;
    // }

    public class AlarmEventType {
        public int eventsNum;
        public String description;
    }

    public class SensorType {
        public Integer type;
        public String description;
    }
}
