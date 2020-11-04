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
    private List<Sensor> sensorsStatus = new ArrayList<Sensor>();
    private List<AlarmEvent> alarmsStatus = new ArrayList<AlarmEvent>();
    private List<bSensorEvent> bSensorsStatus = new ArrayList<bSensorEvent>();

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

    public List<bSensorEvent> getbSensorsStatus() {
        return bSensorsStatus;
    }

    public void setbSensorsStatus(List<bSensorEvent> bSensorsStatus) {
        this.bSensorsStatus = bSensorsStatus;
    }

    public List<AlarmEvent> getAlarmsStatus() {
        return alarmsStatus;
    }

    public void setAlarmsStatus(List<AlarmEvent> alarmsStatus) {
        this.alarmsStatus = alarmsStatus;
    }

    public class Alarm {
        public int event;
        public long ts;
    }

    public class AlarmEvent {
        int alarmType;
        Alarm alarm = new Alarm();
    }

    public class Sensor {
        public int type;
        public float value;
    }

    public class bSensor {
        public long ts;
        public boolean state;
    }

    public class bSensorEvent {
        public int sensorType;
        public bSensor sensor = new bSensor();
    }
}
