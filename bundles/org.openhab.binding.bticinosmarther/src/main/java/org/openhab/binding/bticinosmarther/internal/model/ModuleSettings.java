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
package org.openhab.binding.bticinosmarther.internal.model;

import static org.openhab.binding.bticinosmarther.internal.SmartherBindingConstants.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bticinosmarther.internal.api.dto.Chronothermostat;
import org.openhab.binding.bticinosmarther.internal.api.dto.Enums.BoostTime;
import org.openhab.binding.bticinosmarther.internal.api.dto.Enums.Function;
import org.openhab.binding.bticinosmarther.internal.api.dto.Enums.Mode;
import org.openhab.binding.bticinosmarther.internal.api.exception.SmartherIllegalPropertyValueException;
import org.openhab.binding.bticinosmarther.internal.util.DateUtil;
import org.openhab.binding.bticinosmarther.internal.util.StringUtil;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;

/**
 * The {@code ModuleSettings} class defines the operational settings of a Smarther Chronothermostat.
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

    /**
     * Constructs a {@code ModuleSettings} with the specified plant and module identifiers.
     *
     * @param plantId
     *            the identifier of the plant
     * @param moduleId
     *            the identifier of the chronothermostat module inside the plant
     */
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

    /**
     * Updates this module settings from a {@link Chronothermostat} dto object.
     *
     * @param chronothermostat
     *            the chronothermostat dto to get data from
     * 
     * @throws SmartherIllegalPropertyValueException
     *             if at least one of the module properties cannot be mapped to any valid enum value
     */
    public void updateFromChronothermostat(Chronothermostat chronothermostat)
            throws SmartherIllegalPropertyValueException {
        this.function = Function.fromValue(chronothermostat.getFunction());
    }

    /**
     * Returns the plant identifier.
     *
     * @return a string containing the plant identifier.
     */
    public String getPlantId() {
        return plantId;
    }

    /**
     * Returns the module identifier.
     *
     * @return a string containing the module identifier.
     */
    public String getModuleId() {
        return moduleId;
    }

    /**
     * Returns the module operational function.
     *
     * @return a {@link Function} enum representing the module operational function
     */
    public Function getFunction() {
        return function;
    }

    /**
     * Returns the module operational mode.
     *
     * @return a {@link Mode} enum representing the module operational mode
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Sets the module operational mode.
     *
     * @param mode
     *            a {@link Mode} enum representing the module operational mode to set
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * Returns the module operational setpoint temperature for "manual" mode.
     *
     * @return a {@link QuantityType<Temperature>} object representing the module operational setpoint temperature
     */
    public QuantityType<Temperature> getSetPointTemperature() {
        return setPointTemperature;
    }

    /**
     * Returns the module operational setpoint temperature for "manual" mode, using a target unit.
     *
     * @param targetUnit
     *            the {@link Unit} unit to convert the setpoint temperature to
     *
     * @return a {@link QuantityType<Temperature>} object representing the module operational setpoint temperature
     */
    public @Nullable QuantityType<Temperature> getSetPointTemperature(Unit<?> targetUnit) {
        return setPointTemperature.toUnit(targetUnit);
    }

    /**
     * Sets the module operational setpoint temperature for "manual" mode.
     *
     * @param setPointTemperature
     *            a {@link QuantityType<Temperature>} object representing the setpoint temperature to set
     */
    public void setSetPointTemperature(QuantityType<Temperature> setPointTemperature) {
        this.setPointTemperature = setPointTemperature;
    }

    /**
     * Returns the module operational program for "automatic" mode.
     *
     * @return the module operational program for automatic mode
     */
    public int getProgram() {
        return program;
    }

    /**
     * Sets the module operational program for "automatic" mode.
     *
     * @param program
     *            the module operational program to set
     */
    public void setProgram(int program) {
        this.program = program;
    }

    /**
     * Returns the module operational boost time for "boost" mode.
     *
     * @return a {@link BoostTime} enum representing the module operational boost time
     */
    public BoostTime getBoostTime() {
        return boostTime;
    }

    /**
     * Sets the module operational boost time for "boost" mode.
     *
     * @param boostTime
     *            a {@link BoostTime} enum representing the module operational boost time to set
     */
    public void setBoostTime(BoostTime boostTime) {
        this.boostTime = boostTime;
    }

    /**
     * Returns the module operational end date for "manual" mode.
     *
     * @return a string containing the module operational end date, may be {@code null}
     */
    public @Nullable String getEndDate() {
        return endDate;
    }

    /**
     * Tells whether the module operational end date for "manual" mode has expired.
     *
     * @return {@code true} if the end date has expired, {@code false} otherwise
     */
    public boolean isEndDateExpired() {
        if (endDate != null) {
            final LocalDateTime dtEndDate = DateUtil.parseDate(endDate, DTF_DATE).atStartOfDay();
            final LocalDateTime dtToday = LocalDate.now().atStartOfDay();

            return (dtEndDate.isBefore(dtToday));
        } else {
            return false;
        }
    }

    /**
     * Refreshes the module operational end date for "manual" mode, setting it to current local date.
     */
    public void refreshEndDate() {
        if (endDate != null) {
            this.endDate = DateUtil.format(LocalDateTime.now(), DTF_DATE);
        }
    }

    /**
     * Sets the module operational end date for "manual" mode.
     *
     * @param endDate
     *            the module operational end date to set
     */
    public void setEndDate(String endDate) {
        this.endDate = StringUtil.stripToNull(endDate);
    }

    /**
     * Returns the module operational end hour for "manual" mode.
     *
     * @return the module operational end hour
     */
    public int getEndHour() {
        return endHour;
    }

    /**
     * Sets the module operational end hour for "manual" mode.
     *
     * @param endHour
     *            the module operational end hour to set
     */
    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    /**
     * Returns the module operational end minute for "manual" mode.
     *
     * @return the module operational end minute
     */
    public int getEndMinute() {
        return endMinute;
    }

    /**
     * Sets the module operational end minute for "manual" mode.
     *
     * @param endMinute
     *            the module operational end minute to set
     */
    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    /**
     * Returns the date and time (format YYYY-MM-DDThh:mm:ss) to which this module settings will be maintained.
     * For boost mode a range is returned, as duration is limited to 30, 60 or 90 minutes, indicating starting (current)
     * and final date and time.
     *
     * @return a string containing the module settings activation time, or and empty ("") string if the module operation
     *         mode doesn't allow for an activation time
     */
    public String getActivationTime() {
        if (mode.equals(Mode.MANUAL) && (endDate != null)) {
            LocalDateTime d = DateUtil.parseDate(endDate, DTF_DATE).atTime(endHour, endMinute);
            return DateUtil.format(d, DTF_DATETIME);
        } else if (mode.equals(Mode.BOOST)) {
            LocalDateTime d1 = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
            LocalDateTime d2 = d1.plusMinutes(boostTime.getValue());
            return DateUtil.formatRange(d1, d2, DTF_DATETIME);
        } else {
            return "";
        }
    }

    @Override
    public String toString() {
        return String.format(
                "plantId=%s, moduleId=%s, mode=%s, setPointTemperature=%s, program=%s, boostTime=%s, endDate=%s, endHour=%s, endMinute=%s",
                plantId, moduleId, mode, setPointTemperature, program, boostTime, endDate, endHour, endMinute);
    }
}
