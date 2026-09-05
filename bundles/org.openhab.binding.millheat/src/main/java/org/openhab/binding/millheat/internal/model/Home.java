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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.millheat.internal.dto.HouseDTO;
import org.openhab.binding.millheat.internal.dto.VacationModeRequest;

/**
 * A house, called a home in the binding for continuity with the openHAB thing type. The cloud API
 * returns the complete vacation state as part of the house listing, so no extra request is needed
 * to populate the vacation channels.
 *
 * @author Arne Seime - Initial contribution
 * @author Petter L. H. Eide - Rebuild on the cloud API's house model
 */
@NonNullByDefault
public class Home {
    private final String id;
    private final String name;
    private final @Nullable String timezone;
    private final List<Room> rooms = new ArrayList<>();
    private final List<Heater> independentHeaters = new ArrayList<>();

    private Mode mode;
    private boolean vacationModeActive;
    private @Nullable Instant vacationModeStart;
    private @Nullable Instant vacationModeEnd;
    private @Nullable Double vacationTemperature;
    private @Nullable String vacationModeType;

    public Home(final HouseDTO dto) {
        id = dto.id();
        final String houseName = dto.name();
        name = houseName == null ? dto.id() : houseName;
        timezone = dto.timezone();

        vacationModeActive = Boolean.TRUE.equals(dto.isVacationModeActive());
        vacationModeStart = toInstant(dto.vacationStartDate());
        vacationModeEnd = toInstant(dto.vacationEndDate());
        vacationTemperature = dto.vacationTemperature();
        vacationModeType = dto.vacationModeType();

        mode = vacationModeActive ? new Mode(ModeType.VACATION, vacationModeStart, vacationModeEnd)
                : new Mode(ModeType.fromApiValue(dto.mode()), null, toInstant(dto.overrideEndDate()));
    }

    /** The API uses 0 rather than null for an unset vacation date. */
    private static @Nullable Instant toInstant(final @Nullable Long epochSeconds) {
        return epochSeconds == null || epochSeconds == 0L ? null : Instant.ofEpochSecond(epochSeconds);
    }

    public void addRoom(final Room room) {
        rooms.add(room);
    }

    public void addHeater(final Heater heater) {
        independentHeaters.add(heater);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public @Nullable String getTimezone() {
        return timezone;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public List<Heater> getIndependentHeaters() {
        return independentHeaters;
    }

    public Mode getMode() {
        return mode;
    }

    public boolean isVacationModeActive() {
        return vacationModeActive;
    }

    public void setVacationModeActive(final boolean active) {
        vacationModeActive = active;
        mode = active ? new Mode(ModeType.VACATION, vacationModeStart, vacationModeEnd) : Mode.of(ModeType.UNKNOWN);
    }

    public @Nullable Instant getVacationModeStart() {
        return vacationModeStart;
    }

    public void setVacationModeStart(final @Nullable Instant start) {
        vacationModeStart = start;
    }

    public @Nullable Instant getVacationModeEnd() {
        return vacationModeEnd;
    }

    public void setVacationModeEnd(final @Nullable Instant end) {
        vacationModeEnd = end;
    }

    public @Nullable Double getVacationTemperature() {
        return vacationTemperature;
    }

    public void setVacationTemperature(final @Nullable Double temperature) {
        vacationTemperature = temperature;
    }

    /**
     * On means each room's own away temperature is used, off means the single vacation temperature
     * set for the house. Surfaced as the {@code vacationModeAdvanced} channel.
     */
    public boolean isAdvancedVacationMode() {
        return VacationModeRequest.TYPE_AWAY_TEMPERATURE.equals(vacationModeType);
    }

    public void setAdvancedVacationMode(final boolean advanced) {
        vacationModeType = advanced ? VacationModeRequest.TYPE_AWAY_TEMPERATURE
                : VacationModeRequest.TYPE_VACATION_TEMPERATURE;
    }

    public @Nullable String getVacationModeType() {
        return vacationModeType;
    }

    /**
     * Copies vacation settings staged on a previous snapshot onto this one.
     * <p>
     * The cloud API has no way to store vacation dates without also switching vacation mode on:
     * POST enables it and PATCH requires it to be active already. The documented sequence is to set
     * the start and end times and only then flip the mode, so those times have to survive locally
     * until they are committed. Without this they would be lost to the next poll, which rebuilds
     * every home from a response that reports no dates while vacation is inactive, and enabling
     * would then fail for want of a start and end time.
     */
    public void carryStagedVacationSettings(final Home previous) {
        if (vacationModeActive) {
            // The service is authoritative once vacation is running.
            return;
        }
        if (vacationModeStart == null) {
            vacationModeStart = previous.vacationModeStart;
        }
        if (vacationModeEnd == null) {
            vacationModeEnd = previous.vacationModeEnd;
        }
        if (previous.vacationModeType != null) {
            vacationModeType = previous.vacationModeType;
        }
        if (previous.vacationTemperature != null) {
            vacationTemperature = previous.vacationTemperature;
        }
    }

    @Override
    public String toString() {
        return "Home [id=" + id + ", name=" + name + ", timezone=" + timezone + ", mode=" + mode + ", vacationActive="
                + vacationModeActive + ", rooms=" + rooms.size() + ", independentHeaters=" + independentHeaters.size()
                + "]";
    }
}
