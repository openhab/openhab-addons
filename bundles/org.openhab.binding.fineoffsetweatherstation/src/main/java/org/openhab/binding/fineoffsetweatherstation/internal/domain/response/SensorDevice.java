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
package org.openhab.binding.fineoffsetweatherstation.internal.domain.response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.Sensor;

/**
 * HHolds all available information of a sensor device.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class SensorDevice {
    private final int id;
    private final Sensor sensor;
    private final BatteryStatus batteryStatus;
    private final int signal;

    public SensorDevice(int id, Sensor sensor, BatteryStatus batteryStatus, int signal) {
        this.id = id;
        this.sensor = sensor;
        this.batteryStatus = batteryStatus;
        this.signal = signal;
    }

    public int getId() {
        return id;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public BatteryStatus getBatteryStatus() {
        return batteryStatus;
    }

    public int getSignal() {
        return signal;
    }

    @Override
    public String toString() {
        return "SensorDevice{" + "id=" + id + ", sensor=" + sensor + ", batteryStatus=" + batteryStatus + ", signal="
                + signal + '}';
    }
}
