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

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.millheat.internal.dto.RoomDTO;

/**
 * The {@link Room} represents a room in a home as designed by the end user in the Millheat app.
 *
 * @author Arne Seime - Initial contribution
 */
public class Room {
    private final Home home;
    private final long id;
    private final String name;
    private final int currentTemp;
    private final int comfortTemp;
    private final int sleepTemp;
    private final int awayTemp;
    private final boolean heatingActive;
    private final ModeType mode;
    private final String roomProgramName;
    private final List<Heater> heaters = new ArrayList<>();

    public Room(final RoomDTO dto, final Home home) {
        this.home = home;
        id = dto.roomId;
        name = dto.name;
        currentTemp = (int) dto.currentTemp;
        comfortTemp = dto.comfortTemp;
        sleepTemp = dto.sleepTemp;
        awayTemp = dto.awayTemp;
        heatingActive = dto.heatStatus;
        mode = ModeType.valueOf(dto.currentMode);
        roomProgramName = dto.roomProgram;
    }

    public void addHeater(final Heater h) {
        heaters.add(h);
    }

    public List<Heater> getHeaters() {
        return heaters;
    }

    public Integer getTargetTemperature() {
        switch (mode) {
            case VACATION:
                return home.getHolidayTemp();
            case SLEEP:
                return sleepTemp;
            case COMFORT:
                return comfortTemp;
            case AWAY:
                return awayTemp;
            case OFF:
            case ALWAYSHOME:
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return "Room [home=" + home.getId() + ", id=" + id + ", name=" + name + ", currentTemp=" + currentTemp
                + ", comfortTemp=" + comfortTemp + ", sleepTemp=" + sleepTemp + ", awayTemp=" + awayTemp
                + ", heatingActive=" + heatingActive + ", mode=" + mode + ", roomProgramName=" + roomProgramName
                + ", heaters=" + heaters + "]";
    }

    public Home getHome() {
        return home;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCurrentTemp() {
        return currentTemp;
    }

    public int getComfortTemp() {
        return comfortTemp;
    }

    public int getSleepTemp() {
        return sleepTemp;
    }

    public int getAwayTemp() {
        return awayTemp;
    }

    public boolean isHeatingActive() {
        return heatingActive;
    }

    public ModeType getMode() {
        return mode;
    }

    public String getRoomProgramName() {
        return roomProgramName;
    }
}
