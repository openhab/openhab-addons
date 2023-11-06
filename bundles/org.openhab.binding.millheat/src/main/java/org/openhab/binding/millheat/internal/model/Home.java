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
package org.openhab.binding.millheat.internal.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.millheat.internal.dto.HomeDTO;
import org.openhab.core.library.types.OnOffType;

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
    private int holidayTemp;
    private Mode mode;
    private final String program = null;
    private final List<Room> rooms = new ArrayList<>();
    private final List<Heater> independentHeaters = new ArrayList<>();
    private LocalDateTime vacationModeStart;
    private LocalDateTime vacationModeEnd;
    private boolean advancedVacationMode;

    public Home(final HomeDTO dto) {
        id = dto.homeId;
        name = dto.name;
        type = dto.homeType;
        zoneOffset = dto.timeZone;
        holidayTemp = dto.holidayTemp;
        advancedVacationMode = dto.holidayTempType == 0;
        if (dto.holidayStartTime != 0) {
            vacationModeStart = convertFromEpoch(dto.holidayStartTime);
        }
        if (dto.holidayEndTime != 0) {
            vacationModeEnd = convertFromEpoch(dto.holidayEndTime);
        }

        if (dto.holiday) {
            mode = new Mode(ModeType.VACATION, vacationModeStart, vacationModeEnd);
        } else if (dto.alwaysHome) {
            mode = new Mode(ModeType.ALWAYSHOME, null, null);
        } else {
            final LocalDateTime modeStart = LocalDateTime.ofEpochSecond(dto.modeStartTime, 0,
                    ZoneOffset.of(zoneOffset));
            final LocalDateTime modeEnd = modeStart.withHour(dto.modeHour).withMinute(dto.modeMinute);
            mode = new Mode(ModeType.valueOf(dto.currentMode), modeStart, modeEnd);
        }
    }

    private LocalDateTime convertFromEpoch(long epoch) {
        return LocalDateTime.ofEpochSecond(epoch, 0, ZoneOffset.of(zoneOffset));
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

    public LocalDateTime getVacationModeStart() {
        return vacationModeStart;
    }

    public LocalDateTime getVacationModeEnd() {
        return vacationModeEnd;
    }

    public void setVacationModeStart(long epoch) {
        vacationModeStart = convertFromEpoch(epoch);
        updateVacationMode();
    }

    public void setVacationModeEnd(long epoch) {
        vacationModeEnd = convertFromEpoch(epoch);
        updateVacationMode();
    }

    public void setHolidayTemp(int holidayTemp) {
        this.holidayTemp = holidayTemp;
        updateVacationMode();
    }

    private void updateVacationMode() {
        if (mode.getMode() == ModeType.VACATION) {
            mode = new Mode(ModeType.VACATION, vacationModeStart, vacationModeEnd);
        }
    }

    public void setVacationModeAdvanced(OnOffType command) {
        advancedVacationMode = (OnOffType.ON == command);
    }

    public boolean isAdvancedVacationMode() {
        return advancedVacationMode;
    }

    public void setAdvancedVacationMode(boolean advancedVacationMode) {
        this.advancedVacationMode = advancedVacationMode;
    }
}
