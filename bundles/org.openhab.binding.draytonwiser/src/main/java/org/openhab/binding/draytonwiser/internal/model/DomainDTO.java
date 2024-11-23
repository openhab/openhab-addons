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
package org.openhab.binding.draytonwiser.internal.model;

import java.util.List;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class DomainDTO {

    private SystemDTO system;
    private List<HeatingChannelDTO> heatingChannel;
    private List<HotWaterDTO> hotWater;
    private List<RoomDTO> room;
    private List<DeviceDTO> device;
    private List<SmartValveDTO> smartValve;
    private List<RoomStatDTO> roomStat;
    private List<SmartPlugDTO> smartPlug;

    public SystemDTO getSystem() {
        return system;
    }

    public void setSystem(final SystemDTO system) {
        this.system = system;
    }

    public List<HeatingChannelDTO> getHeatingChannel() {
        return heatingChannel;
    }

    public void setHeatingChannel(final List<HeatingChannelDTO> heatingChannel) {
        this.heatingChannel = heatingChannel;
    }

    public List<HotWaterDTO> getHotWater() {
        return hotWater;
    }

    public void setHotWater(final List<HotWaterDTO> hotWater) {
        this.hotWater = hotWater;
    }

    public List<RoomDTO> getRoom() {
        return room;
    }

    public void setRoom(final List<RoomDTO> room) {
        this.room = room;
    }

    public List<DeviceDTO> getDevice() {
        return device;
    }

    public void setDevice(final List<DeviceDTO> device) {
        this.device = device;
    }

    public List<SmartValveDTO> getSmartValve() {
        return smartValve;
    }

    public void setSmartValve(final List<SmartValveDTO> smartValve) {
        this.smartValve = smartValve;
    }

    public List<RoomStatDTO> getRoomStat() {
        return roomStat;
    }

    public void setRoomStat(final List<RoomStatDTO> roomStat) {
        this.roomStat = roomStat;
    }

    public List<SmartPlugDTO> getSmartPlug() {
        return smartPlug;
    }

    public void setSmartPlug(final List<SmartPlugDTO> smartPlug) {
        this.smartPlug = smartPlug;
    }
}
