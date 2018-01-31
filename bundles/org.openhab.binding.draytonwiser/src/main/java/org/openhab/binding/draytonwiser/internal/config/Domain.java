/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

}
