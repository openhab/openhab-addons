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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TouchWandAlarmSensorCurrentStatus} implements Alarm Sensor unit
 * CurrentStatus data property.
 *
 * @author Roie Geron - Initial contribution
 */

@NonNullByDefault
public class TouchWandAlarmSensorCurrentStatus {

    private int batt;
    private List<Sensor> sensorsStatus = new ArrayList<>();
    private List<AlarmEvent> alarmsStatus = new ArrayList<>();
    private List<BinarySensorEvent> bSensorsStatus = new ArrayList<>();

    public void setBatt(Integer batt) {
        this.batt = batt;
    }

    public int getBatt() {
        return batt;
    }

    public void setSensorsStatus(List<Sensor> sensorsStatus) {
        this.sensorsStatus = sensorsStatus;
    }

    public List<Sensor> getSensorsStatus() {
        return sensorsStatus;
    }

    public List<BinarySensorEvent> getbSensorsStatus() {
        return bSensorsStatus;
    }

    public void setbSensorsStatus(List<BinarySensorEvent> bSensorsStatus) {
        this.bSensorsStatus = bSensorsStatus;
    }

    public List<AlarmEvent> getAlarmsStatus() {
        return alarmsStatus;
    }

    public void setAlarmsStatus(List<AlarmEvent> alarmsStatus) {
        this.alarmsStatus = alarmsStatus;
    }

    public static class Alarm {
        public int event;
        public long ts;
    }

    public static class AlarmEvent {
        int alarmType;
        Alarm alarm = new Alarm();
    }

    public static class Sensor {
        public int type;
        public float value;
    }

    public static class BinarySensor {
        public long ts;
        public boolean state;
    }

    public static class BinarySensorEvent {
        public int sensorType;
        public BinarySensor sensor = new BinarySensor();
    }
}
