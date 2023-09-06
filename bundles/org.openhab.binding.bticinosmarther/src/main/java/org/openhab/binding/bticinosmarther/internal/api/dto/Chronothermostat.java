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
package org.openhab.binding.bticinosmarther.internal.api.dto;

import static org.openhab.binding.bticinosmarther.internal.SmartherBindingConstants.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bticinosmarther.internal.api.dto.Enums.LoadState;
import org.openhab.binding.bticinosmarther.internal.api.dto.Enums.MeasureUnit;
import org.openhab.binding.bticinosmarther.internal.api.exception.SmartherIllegalPropertyValueException;
import org.openhab.binding.bticinosmarther.internal.util.DateUtil;

import com.google.gson.annotations.SerializedName;

/**
 * The {@code Chronothermostat} class defines the dto for Smarther API chronothermostat object.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class Chronothermostat {

    private static final String TIME_FOREVER = "Forever";

    private String function;
    private String mode;
    @SerializedName("setPoint")
    private Measure setPointTemperature;
    private List<Program> programs;
    @SerializedName("temperatureFormat")
    private String temperatureFormat;
    @SerializedName("loadState")
    private String loadState;
    @SerializedName("activationTime")
    private String activationTime;
    private String time;
    private Sensor thermometer;
    private Sensor hygrometer;
    private boolean online;
    private Sender sender;

    /**
     * Returns the operational function of this chronothermostat module.
     *
     * @return a string containing the module operational function
     */
    public String getFunction() {
        return function;
    }

    /**
     * Returns the operational mode of this chronothermostat module.
     *
     * @return a string containing the module operational mode
     */
    public String getMode() {
        return mode;
    }

    /**
     * Returns the operational setpoint temperature of this chronothermostat module.
     *
     * @return a {@link Measure} object representing the module operational setpoint temperature
     */
    public Measure getSetPointTemperature() {
        return setPointTemperature;
    }

    /**
     * Returns the list of programs registered on this chronothermostat module.
     *
     * @return the list of registered programs, or an empty list in case of no programs available
     */
    public List<Program> getPrograms() {
        return (programs != null) ? programs : Collections.emptyList();
    }

    /**
     * Returns the operational temperature format of this chronothermostat module.
     *
     * @return a string containing the module operational temperature format
     */
    public String getTemperatureFormat() {
        return temperatureFormat;
    }

    /**
     * Returns the operational temperature format of this chronothermostat module.
     *
     * @return a {@link MeasureUnit} object representing the module operational temperature format
     *
     * @throws {@link SmartherIllegalPropertyValueException}
     *             if the measure internal raw unit cannot be mapped to any valid measure unit
     */
    public MeasureUnit getTemperatureFormatUnit() throws SmartherIllegalPropertyValueException {
        return MeasureUnit.fromValue(temperatureFormat);
    }

    /**
     * Returns the operational load state of this chronothermostat module.
     *
     * @return a string containing the module operational load state
     */
    public String getLoadState() {
        return loadState;
    }

    /**
     * Tells whether the load state of this chronothermostat module is "active" (i.e. module is turned on).
     *
     * @return {@code true} if the load state is active, {@code false} otherwise
     *
     * @throws {@link SmartherIllegalPropertyValueException}
     *             if the load state internal raw value cannot be mapped to any valid load state enum value
     */
    public boolean isActive() throws SmartherIllegalPropertyValueException {
        return LoadState.fromValue(loadState).isActive();
    }

    /**
     * Returns the operational activation time of this chronothermostat module.
     *
     * @return a string containing the module operational activation time
     */
    public String getActivationTime() {
        return activationTime;
    }

    /**
     * Returns a label for the operational activation time of this chronothermostat module.
     *
     * @return a string containing the module operational activation time label, or {@code null} if the activation time
     *         cannot be parsed to a valid date/time
     */
    public @Nullable String getActivationTimeLabel() {
        String timeLabel = TIME_FOREVER;
        if (activationTime != null) {
            try {
                final ZonedDateTime dateActivationTime = DateUtil.parseZonedTime(activationTime, DTF_DATETIME_EXT);
                final ZonedDateTime dateTomorrow = DateUtil.getZonedStartOfDay(1, dateActivationTime.getZone());

                if (dateActivationTime.isBefore(dateTomorrow)) {
                    timeLabel = DateUtil.format(dateActivationTime, DTF_TODAY);
                } else if (dateActivationTime.isBefore(dateTomorrow.plusDays(1))) {
                    timeLabel = DateUtil.format(dateActivationTime, DTF_TOMORROW);
                } else {
                    timeLabel = DateUtil.format(dateActivationTime, DTF_DAY_HHMM);
                }
            } catch (DateTimeParseException e) {
                timeLabel = null;
            }
        }
        return timeLabel;
    }

    /**
     * Returns the current time (clock) of this chronothermostat module.
     *
     * @return a string containing the module current time
     */
    public String getTime() {
        return time;
    }

    /**
     * Returns the thermometer sensor of this chronothermostat module.
     *
     * @return the thermometer sensor of this module
     */
    public Sensor getThermometer() {
        return thermometer;
    }

    /**
     * Returns the hygrometer sensor of this chronothermostat module.
     *
     * @return the hygrometer sensor of this module
     */
    public Sensor getHygrometer() {
        return hygrometer;
    }

    /**
     * Tells whether this module is online.
     *
     * @return {@code true} if the module is online, {@code false} otherwise
     */
    public boolean isOnline() {
        return online;
    }

    /**
     * Returns the sender associated with this chronothermostat module.
     *
     * @return a {@link Sender} object representing the sender associated with this module, or {@code null} in case of
     *         no sender information available
     */
    public @Nullable Sender getSender() {
        return sender;
    }

    /**
     * Returns the operational program of this chronothermostat module.
     *
     * @return a {@link Program} object representing the module operational program, or {@code null} in case of no
     *         program currently set for this module
     */
    public @Nullable Program getProgram() {
        return (programs != null && !programs.isEmpty()) ? programs.get(0) : null;
    }

    @Override
    public String toString() {
        return String.format(
                "function=%s, mode=%s, setPointTemperature=[%s], programs=%s, temperatureFormat=%s, loadState=%s, time=%s, activationTime=%s, thermometer=[%s], hygrometer=[%s], online=%s, sender=[%s]",
                function, mode, setPointTemperature, programs, temperatureFormat, loadState, time, activationTime,
                thermometer, hygrometer, online, sender);
    }
}
