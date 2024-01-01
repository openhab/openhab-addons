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
package org.openhab.binding.millheat.internal.model;

import org.openhab.binding.millheat.internal.dto.DeviceDTO;

/**
 * The {@link Heater} represents a heater, either connected to a room or independent
 *
 * @author Arne Seime - Initial contribution
 */
public class Heater {
    private Room room;
    private final Long id;
    private final String name;
    private final String macAddress;
    private final boolean heatingActive;
    private boolean canChangeTemp = true;
    private final int subDomain;
    private final int currentTemp;
    private Integer targetTemp;
    private boolean fanActive;
    private boolean powerStatus;
    private final boolean windowOpen;

    public Heater(final DeviceDTO dto) {
        id = dto.deviceId;
        name = dto.deviceName;
        macAddress = dto.macAddress;
        heatingActive = dto.heaterFlag;
        canChangeTemp = dto.canChangeTemp;
        subDomain = dto.subDomainId;
        currentTemp = (int) dto.currentTemp;
        setTargetTemp(dto.holidayTemp);
        setFanActive(dto.fanStatus);
        setPowerStatus(dto.powerStatus);
        windowOpen = dto.openWindow;
    }

    public Heater(final DeviceDTO dto, final Room room) {
        this.room = room;
        id = dto.deviceId;
        name = dto.deviceName;
        macAddress = dto.macAddress;
        heatingActive = dto.heaterFlag;
        canChangeTemp = dto.canChangeTemp;
        subDomain = dto.subDomainId;
        currentTemp = (int) dto.currentTemp;
        if (room != null && room.getMode() != null) {
            switch (room.getMode()) {
                case COMFORT:
                    setTargetTemp(room.getComfortTemp());
                    break;
                case SLEEP:
                    setTargetTemp(room.getSleepTemp());
                    break;
                case AWAY:
                    setTargetTemp(room.getAwayTemp());
                    break;
                case OFF:
                    setTargetTemp(null);
                    break;
                default:
                    // NOOP
            }
        }
        setFanActive(dto.fanStatus);
        setPowerStatus(dto.powerStatus);
        windowOpen = dto.openWindow;
    }

    @Override
    public String toString() {
        return "Heater [room=" + room + ", id=" + id + ", name=" + name + ", macAddress=" + macAddress
                + ", heatingActive=" + heatingActive + ", canChangeTemp=" + canChangeTemp + ", subDomain=" + subDomain
                + ", currentTemp=" + currentTemp + ", targetTemp=" + getTargetTemp() + ", fanActive=" + fanActive()
                + ", powerStatus=" + powerStatus() + ", windowOpen=" + windowOpen + "]";
    }

    public Room getRoom() {
        return room;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public boolean isHeatingActive() {
        return heatingActive;
    }

    public boolean canChangeTemp() {
        return canChangeTemp;
    }

    public int getSubDomain() {
        return subDomain;
    }

    public int getCurrentTemp() {
        return currentTemp;
    }

    public Integer getTargetTemp() {
        return targetTemp;
    }

    public boolean fanActive() {
        return fanActive;
    }

    public boolean powerStatus() {
        return powerStatus;
    }

    public boolean windowOpen() {
        return windowOpen;
    }

    public void setTargetTemp(final Integer targetTemp) {
        this.targetTemp = targetTemp;
    }

    public void setFanActive(final boolean fanActive) {
        this.fanActive = fanActive;
    }

    public void setPowerStatus(final boolean powerStatus) {
        this.powerStatus = powerStatus;
    }
}
