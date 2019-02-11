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

package org.openhab.binding.draytonwiser.internal.config;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class Domain {

    @SerializedName("System")
    @Expose
    private System system;
    @SerializedName("HeatingChannel")
    @Expose
    private List<HeatingChannel> heatingChannel = null;
    @SerializedName("HotWater")
    @Expose
    private List<HotWater> hotWater = null;
    @SerializedName("Room")
    @Expose
    private List<Room> room = null;
    @SerializedName("Schedule")
    @Expose
    private List<Schedule> schedule = null;
    @SerializedName("Device")
    @Expose
    private List<Device> device = null;
    @SerializedName("SmartValve")
    @Expose
    private List<SmartValve> smartValve = null;
    @SerializedName("RoomStat")
    @Expose
    private List<RoomStat> roomStat = null;
    @SerializedName("SmartPlug")
    @Expose
    private List<SmartPlug> smartPlug = null;

    public System getSystem() {
        return system;
    }

    public void setSystem(System system) {
        this.system = system;
    }

    public List<HeatingChannel> getHeatingChannel() {
        return heatingChannel;
    }

    public void setHeatingChannel(List<HeatingChannel> heatingChannel) {
        this.heatingChannel = heatingChannel;
    }

    public List<HotWater> getHotWater() {
        return hotWater;
    }

    public void setHotWater(List<HotWater> hotWater) {
        this.hotWater = hotWater;
    }

    public List<Room> getRoom() {
        return room;
    }

    public void setRoom(List<Room> room) {
        this.room = room;
    }

    public List<Schedule> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<Schedule> schedule) {
        this.schedule = schedule;
    }

    public List<Device> getDevice() {
        return device;
    }

    public void setDevice(List<Device> device) {
        this.device = device;
    }

    public List<SmartValve> getSmartValve() {
        return smartValve;
    }

    public void setSmartValve(List<SmartValve> smartValve) {
        this.smartValve = smartValve;
    }

    public List<RoomStat> getRoomStat() {
        return roomStat;
    }

    public void setRoomStat(List<RoomStat> roomStat) {
        this.roomStat = roomStat;
    }

    public List<SmartPlug> getSmartPlug() {
        return smartPlug;
    }

}
