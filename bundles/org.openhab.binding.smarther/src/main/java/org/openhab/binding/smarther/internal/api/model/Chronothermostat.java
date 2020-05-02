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
package org.openhab.binding.smarther.internal.api.model;

import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.openhab.binding.smarther.internal.api.model.Enums.LoadState;
import org.openhab.binding.smarther.internal.api.model.Enums.MeasureUnit;

import com.google.gson.annotations.SerializedName;

/**
 * Smarther API Chronothermostat DTO class.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class Chronothermostat {

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

    public String getFunction() {
        return function;
    }

    public String getMode() {
        return mode;
    }

    public Measure getSetPointTemperature() {
        return setPointTemperature;
    }

    public List<Program> getPrograms() {
        return (programs != null) ? programs : Collections.emptyList();
    }

    public String getTemperatureFormat() {
        return temperatureFormat;
    }

    public MeasureUnit getTemperatureFormatUnit() {
        return MeasureUnit.fromValue(temperatureFormat);
    }

    public boolean isTemperatureFormatCelsius() {
        return MeasureUnit.CELSIUS.getValue().equals(temperatureFormat);
    }

    public boolean isTemperatureFormatFahrenheit() {
        return MeasureUnit.FAHRENHEIT.getValue().equals(temperatureFormat);
    }

    public String getLoadState() {
        return loadState;
    }

    public boolean isActive() {
        return LoadState.fromValue(loadState).isActive();
    }

    public String getActivationTime() {
        return activationTime;
    }

    public String getActivationTimeLabel() {
        String timeLabel = "Forever";
        if (activationTime != null) {
            final DateTime dateActivationTime = new DateTime(activationTime);
            final DateTime dateTomorrow = DateTime.now().plusDays(1).withTimeAtStartOfDay();

            if (dateActivationTime.isBefore(dateTomorrow)) {
                timeLabel = dateActivationTime.toString("'Today at' HH:mm");
            } else if (dateActivationTime.isBefore(dateTomorrow.plusDays(1))) {
                timeLabel = dateActivationTime.toString("'Tomorrow at' HH:mm");
            } else {
                timeLabel = dateActivationTime.toString("dd/MM/yyyy 'at' HH:mm");
            }
        }
        return timeLabel;
    }

    public String getTime() {
        return time;
    }

    public Sensor getThermometer() {
        return thermometer;
    }

    public Sensor getHygrometer() {
        return hygrometer;
    }

    public boolean isOnline() {
        return online;
    }

    public Sender getSender() {
        return sender;
    }

    public boolean hasSender() {
        return (sender != null && sender.getPlant() != null);
    }

    public Program getProgram() {
        return (!programs.isEmpty()) ? programs.get(0) : null;
    }

    @Override
    public String toString() {
        return String.format(
                "function=%s, mode=%s, setPointTemperature=[%s], programs=%s, temperatureFormat=%s, loadState=%s, time=%s, activationTime=%s, thermometer=[%s], hygrometer=[%s], online=%s, sender=[%s]",
                function, mode, setPointTemperature, programs, temperatureFormat, loadState, time, activationTime,
                thermometer, hygrometer, online, sender);
    }

}
