/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.millheat.internal.dto.RoomInfoDTO;

/**
 * A room as laid out by the user in the Mill app. Rooms carry the three program setpoints; the
 * heaters in a room follow whichever one the active mode selects.
 *
 * @author Arne Seime - Initial contribution
 * @author Petter L. H. Eide - Rebuild on the cloud API's room model
 */
@NonNullByDefault
public class Room {
    private final Home home;
    private final String id;
    private final String name;
    private final @Nullable Double currentTemp;
    private final @Nullable Double comfortTemp;
    private final @Nullable Double sleepTemp;
    private final @Nullable Double awayTemp;
    private final boolean heatingActive;
    private final boolean windowOpen;
    private final boolean online;
    private final ModeType mode;
    private final @Nullable String roomProgramName;
    private final List<Heater> heaters = new ArrayList<>();

    public Room(final RoomInfoDTO dto, final Home home) {
        this.home = home;
        id = dto.id();
        final String roomName = dto.name();
        name = roomName == null ? dto.id() : roomName;
        currentTemp = dto.averageTemperature();
        comfortTemp = dto.roomComfortTemperature();
        sleepTemp = dto.roomSleepTemperature();
        awayTemp = dto.roomAwayTemperature();
        heatingActive = Boolean.TRUE.equals(dto.roomHeatStatus());
        windowOpen = Boolean.TRUE.equals(dto.roomOpenWindowStatus());
        online = Boolean.TRUE.equals(dto.isRoomOnline());
        roomProgramName = dto.roomProgramName();

        // While a weekly program is running, the effective mode is the one the program selected.
        final ModeType declared = ModeType.fromApiValue(dto.mode());
        mode = declared == ModeType.WEEKLY_PROGRAM ? ModeType.fromApiValue(dto.activeModeFromWeeklyProgram())
                : declared;
    }

    public Room(final String id, final String name, final Home home) {
        this.home = home;
        this.id = id;
        this.name = name;
        currentTemp = null;
        comfortTemp = null;
        sleepTemp = null;
        awayTemp = null;
        heatingActive = false;
        windowOpen = false;
        online = false;
        mode = ModeType.UNKNOWN;
        roomProgramName = null;
    }

    public void addHeater(final Heater heater) {
        heaters.add(heater);
    }

    public List<Heater> getHeaters() {
        return heaters;
    }

    public @Nullable Double getTargetTemperature() {
        return switch (mode) {
            case COMFORT, NORMAL -> comfortTemp;
            case SLEEP -> sleepTemp;
            case AWAY -> awayTemp;
            case VACATION -> home.getVacationTemperature();
            default -> null;
        };
    }

    public Home getHome() {
        return home;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public @Nullable Double getCurrentTemp() {
        return currentTemp;
    }

    public @Nullable Double getComfortTemp() {
        return comfortTemp;
    }

    public @Nullable Double getSleepTemp() {
        return sleepTemp;
    }

    public @Nullable Double getAwayTemp() {
        return awayTemp;
    }

    public boolean isHeatingActive() {
        return heatingActive;
    }

    public boolean windowOpen() {
        return windowOpen;
    }

    public boolean isOnline() {
        return online;
    }

    public ModeType getMode() {
        return mode;
    }

    public @Nullable String getRoomProgramName() {
        return roomProgramName;
    }

    @Override
    public String toString() {
        return "Room [id=" + id + ", name=" + name + ", home=" + home.getId() + ", currentTemp=" + currentTemp
                + ", comfort=" + comfortTemp + ", sleep=" + sleepTemp + ", away=" + awayTemp + ", mode=" + mode
                + ", program=" + roomProgramName + ", heaters=" + heaters.size() + "]";
    }
}
