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
package org.openhab.binding.millheat.internal.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.millheat.internal.dto.HomeDTO;

/**
 * The {@link Home} represents a home
 *
 * @author Arne Seime - Initial contribution
 */
public class Home {
    private final long id;
    private final String name;
    private final int type;
    private final String zoneOffset;
    private final int holidayTemp;
    private Mode mode;
    private final String program = null;
    private final List<Room> rooms = new ArrayList<>();
    private final List<Heater> independentHeaters = new ArrayList<>();

    public Home(final HomeDTO dto) {
        id = dto.homeId;
        name = dto.name;
        type = dto.homeType;
        zoneOffset = dto.timeZone;
        holidayTemp = dto.holidayTemp;
        if (dto.holiday) {
            final LocalDateTime modeStart = LocalDateTime.ofEpochSecond(dto.holidayStartTime, 0,
                    ZoneOffset.of(zoneOffset));
            final LocalDateTime modeEnd = LocalDateTime.ofEpochSecond(dto.holidayEndTime, 0, ZoneOffset.of(zoneOffset));
            mode = new Mode(ModeType.VACATION, modeStart, modeEnd);
        } else if (dto.alwaysHome) {
            mode = new Mode(ModeType.ALWAYSHOME, null, null);
        } else {
            final LocalDateTime modeStart = LocalDateTime.ofEpochSecond(dto.modeStartTime, 0,
                    ZoneOffset.of(zoneOffset));
            final LocalDateTime modeEnd = modeStart.withHour(dto.modeHour).withMinute(dto.modeMinute);
            mode = new Mode(ModeType.valueOf(dto.currentMode), modeStart, modeEnd);
        }
    }

    public void addRoom(final Room room) {
        rooms.add(room);
    }

    public void addHeater(final Heater heater) {
        independentHeaters.add(heater);
    }

    @Override
    public String toString() {
        return "Home [id=" + id + ", name=" + name + ", type=" + type + ", zoneOffset=" + zoneOffset + ", holidayTemp="
                + holidayTemp + ", mode=" + mode + ", rooms=" + rooms + ", independentHeaters=" + independentHeaters
                + ", program=" + program + "]";
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public String getTimezone() {
        return zoneOffset;
    }

    public int getHolidayTemp() {
        return holidayTemp;
    }

    public Mode getMode() {
        return mode;
    }

    public String getProgram() {
        return program;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public List<Heater> getIndependentHeaters() {
        return independentHeaters;
    }
}
