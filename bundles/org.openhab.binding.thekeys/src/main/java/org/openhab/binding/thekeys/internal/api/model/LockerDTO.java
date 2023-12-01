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
package org.openhab.binding.thekeys.internal.api.model;

/**
 * Child class of response DTO for /lockers endpoint
 *
 * @author Jordan Martin - Initial contribution
 */
public class LockerDTO {
    private int identifier;
    private int rssi;
    private int battery;
    private int lastLog;

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public int getLastLog() {
        return lastLog;
    }

    public void setLastLog(int lastLog) {
        this.lastLog = lastLog;
    }
}
