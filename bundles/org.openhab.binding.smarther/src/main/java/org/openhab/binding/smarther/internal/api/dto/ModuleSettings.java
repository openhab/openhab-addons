/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.smarther.internal.api.dto;

import static org.openhab.binding.smarther.internal.SmartherBindingConstants.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.openhab.binding.smarther.internal.api.dto.Enums.BoostTime;
import org.openhab.binding.smarther.internal.api.dto.Enums.Function;
import org.openhab.binding.smarther.internal.api.dto.Enums.Mode;
import org.openhab.binding.smarther.internal.util.DateUtil;
import org.openhab.binding.smarther.internal.util.StringUtil;

/**
 * Smarther API ModuleSettings update data class.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class ModuleSettings {

    private transient String plantId;
    private transient String moduleId;
    private Function function;
    private Mode mode;
    private QuantityType<Temperature> setPointTemperature;
    private int program;
    private BoostTime boostTime;
    private @Nullable String endDate;
    private int endHour;
    private int endMinute;

    public ModuleSettings(String plantId, String moduleId) {
        this.plantId = plantId;
        this.moduleId = moduleId;
        this.function = Function.HEATING;
        this.mode = Mode.AUTOMATIC;
        this.setPointTemperature = QuantityType.valueOf(7.0, SIUnits.CELSIUS);
        this.program = 0;
        this.boostTime = BoostTime.MINUTES_30;
        this.endDate = null;
        this.endHour = 0;
        this.endMinute = 0;
    }

    public void updateFromChronothermostat(Chronothermostat chronothermostat) {
        this.function = Function.fromValue(chronothermostat.getFunction());
    }

    public String getPlantId() {
        return plantId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public Function getFunction() {
        return function;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public QuantityType<Temperature> getSetPointTemperature() {
        return setPointTemperature;
    }

    @Nullable
    public QuantityType<Temperature> getSetPointTemperature(Unit<?> targetUnit) {
        return setPointTemperature.toUnit(targetUnit);
    }

    public void setSetPointTemperature(QuantityType<Temperature> setPointTemperature) {
        this.setPointTemperature = setPointTemperature;
    }

    public int getProgram() {
        return program;
    }

    public void setProgram(int program) {
        this.program = program;
    }

    public BoostTime getBoostTime() {
        return boostTime;
    }

    public void setBoostTime(BoostTime boostTime) {
        this.boostTime = boostTime;
    }

    @Nullable
    public String getEndDate() {
        return endDate;
    }

    public boolean isEndDateExpired() {
        if (endDate != null) {
            final LocalDateTime dtEndDate = DateUtil.parse(endDate, DTF_DATE).toLocalDate().atStartOfDay();
            final LocalDateTime dtToday = LocalDate.now().atStartOfDay();

            return (dtEndDate.isBefore(dtToday));
        } else {
            return false;
        }
    }

    public void setEndDate(String endDate) {
        this.endDate = StringUtil.stripToNull(endDate);
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    public String getActivationTime() {
        if (mode.equals(Mode.MANUAL) && (endDate != null)) {
            LocalDateTime d = DateUtil.parse(endDate, DTF_DATE).truncatedTo(ChronoUnit.MINUTES);
            return DateUtil.format(d, DTF_DATETIME);
        } else if (mode.equals(Mode.BOOST)) {
            LocalDateTime d1 = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
            LocalDateTime d2 = d1.plusMinutes(boostTime.getValue());
            return DateUtil.formatRange(d1, d2, DTF_DATETIME);
        }

        return "";
    }

    @Override
    public String toString() {
        return String.format(
                "plantId=%s, moduleId=%s, mode=%s, setPointTemperature=%s, program=%s, boostTime=%s, endDate=%s, endHour=%s, endMinute=%s",
                plantId, moduleId, mode, setPointTemperature, program, boostTime, endDate, endHour, endMinute);
    }

}
