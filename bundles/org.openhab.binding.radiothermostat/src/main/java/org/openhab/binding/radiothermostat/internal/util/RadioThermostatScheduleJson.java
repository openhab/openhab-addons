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
package org.openhab.binding.radiothermostat.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.radiothermostat.internal.RadioThermostatConfiguration;

/**
 * The {@link RadioThermostatScheduleJson} is the class used to convert the heating and cooling schedules from user
 * configuration into the json that is sent to the thermostat
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class RadioThermostatScheduleJson {
    private final @Nullable String monHeat;
    private final @Nullable String tueHeat;
    private final @Nullable String wedHeat;
    private final @Nullable String thuHeat;
    private final @Nullable String friHeat;
    private final @Nullable String satHeat;
    private final @Nullable String sunHeat;

    private final @Nullable String monCool;
    private final @Nullable String tueCool;
    private final @Nullable String wedCool;
    private final @Nullable String thuCool;
    private final @Nullable String friCool;
    private final @Nullable String satCool;
    private final @Nullable String sunCool;

    public RadioThermostatScheduleJson(RadioThermostatConfiguration config) {
        monHeat = getDaySchedule(config.monMorningHeatTime, config.monDayHeatTime, config.monEveningHeatTime,
                config.monNightHeatTime, config.monMorningHeatTemp, config.monDayHeatTemp, config.monEveningHeatTemp,
                config.monNightHeatTemp);
        tueHeat = getDaySchedule(config.tueMorningHeatTime, config.tueDayHeatTime, config.tueEveningHeatTime,
                config.tueNightHeatTime, config.tueMorningHeatTemp, config.tueDayHeatTemp, config.tueEveningHeatTemp,
                config.tueNightHeatTemp);
        wedHeat = getDaySchedule(config.wedMorningHeatTime, config.wedDayHeatTime, config.wedEveningHeatTime,
                config.wedNightHeatTime, config.wedMorningHeatTemp, config.wedDayHeatTemp, config.wedEveningHeatTemp,
                config.wedNightHeatTemp);
        thuHeat = getDaySchedule(config.thuMorningHeatTime, config.thuDayHeatTime, config.thuEveningHeatTime,
                config.thuNightHeatTime, config.thuMorningHeatTemp, config.thuDayHeatTemp, config.thuEveningHeatTemp,
                config.thuNightHeatTemp);
        friHeat = getDaySchedule(config.friMorningHeatTime, config.friDayHeatTime, config.friEveningHeatTime,
                config.friNightHeatTime, config.friMorningHeatTemp, config.friDayHeatTemp, config.friEveningHeatTemp,
                config.friNightHeatTemp);
        satHeat = getDaySchedule(config.satMorningHeatTime, config.satDayHeatTime, config.satEveningHeatTime,
                config.satNightHeatTime, config.satMorningHeatTemp, config.satDayHeatTemp, config.satEveningHeatTemp,
                config.satNightHeatTemp);
        sunHeat = getDaySchedule(config.sunMorningHeatTime, config.sunDayHeatTime, config.sunEveningHeatTime,
                config.sunNightHeatTime, config.sunMorningHeatTemp, config.sunDayHeatTemp, config.sunEveningHeatTemp,
                config.sunNightHeatTemp);

        monCool = getDaySchedule(config.monMorningCoolTime, config.monDayCoolTime, config.monEveningCoolTime,
                config.monNightCoolTime, config.monMorningCoolTemp, config.monDayCoolTemp, config.monEveningCoolTemp,
                config.monNightCoolTemp);
        tueCool = getDaySchedule(config.tueMorningCoolTime, config.tueDayCoolTime, config.tueEveningCoolTime,
                config.tueNightCoolTime, config.tueMorningCoolTemp, config.tueDayCoolTemp, config.tueEveningCoolTemp,
                config.tueNightCoolTemp);
        wedCool = getDaySchedule(config.wedMorningCoolTime, config.wedDayCoolTime, config.wedEveningCoolTime,
                config.wedNightCoolTime, config.wedMorningCoolTemp, config.wedDayCoolTemp, config.wedEveningCoolTemp,
                config.wedNightCoolTemp);
        thuCool = getDaySchedule(config.thuMorningCoolTime, config.thuDayCoolTime, config.thuEveningCoolTime,
                config.thuNightCoolTime, config.thuMorningCoolTemp, config.thuDayCoolTemp, config.thuEveningCoolTemp,
                config.thuNightCoolTemp);
        friCool = getDaySchedule(config.friMorningCoolTime, config.friDayCoolTime, config.friEveningCoolTime,
                config.friNightCoolTime, config.friMorningCoolTemp, config.friDayCoolTemp, config.friEveningCoolTemp,
                config.friNightCoolTemp);
        satCool = getDaySchedule(config.satMorningCoolTime, config.satDayCoolTime, config.satEveningCoolTime,
                config.satNightCoolTime, config.satMorningCoolTemp, config.satDayCoolTemp, config.satEveningCoolTemp,
                config.satNightCoolTemp);
        sunCool = getDaySchedule(config.sunMorningCoolTime, config.sunDayCoolTime, config.sunEveningCoolTime,
                config.sunNightCoolTime, config.sunMorningCoolTemp, config.sunDayCoolTemp, config.sunEveningCoolTemp,
                config.sunNightCoolTemp);
    }

    public String getHeatProgramJson() throws IllegalStateException {
        return getProgramJson(monHeat, tueHeat, wedHeat, thuHeat, friHeat, satHeat, sunHeat);
    }

    public String getCoolProgramJson() throws IllegalStateException {
        return getProgramJson(monCool, tueCool, wedCool, thuCool, friCool, satCool, sunCool);
    }

    private String getProgramJson(@Nullable String mon, @Nullable String tue, @Nullable String wed,
            @Nullable String thu, @Nullable String fri, @Nullable String sat, @Nullable String sun)
            throws IllegalStateException {
        // all were null, bypass
        if (mon == null && tue == null && wed == null && thu == null && fri == null && sat == null && sun == null) {
            return "";
        }

        // some were null, the schedule is invalid
        if (mon == null || tue == null || wed == null || thu == null || fri == null || sat == null || sun == null) {
            throw new IllegalStateException();
        }

        return "{\"0\":" + mon + ",\"1\":" + tue + ",\"2\":" + wed + ",\"3\":" + thu + ",\"4\":" + fri + ",\"5\":" + sat
                + ",\"6\":" + sun + "}";
    }

    private @Nullable String getDaySchedule(@Nullable String morningTime, @Nullable String dayTime,
            @Nullable String eveningTime, @Nullable String nightTime, @Nullable Integer morningTemp,
            @Nullable Integer dayTemp, @Nullable Integer eveningTemp, @Nullable Integer nightTemp) {
        // if any null, this day schedule is not valid
        if (morningTime == null || dayTime == null || eveningTime == null || nightTime == null || morningTemp == null
                || dayTemp == null || eveningTemp == null || nightTemp == null) {
            return null;
        }

        final int morningMin;
        final int dayMin;
        final int eveningMin;
        final int nightMin;

        try {
            morningMin = parseMinutes(morningTime);
            dayMin = parseMinutes(dayTime);
            eveningMin = parseMinutes(eveningTime);
            nightMin = parseMinutes(nightTime);
        } catch (NumberFormatException nfe) {
            // if any of the times could not be parsed into minutes, the schedule is invalid
            return null;
        }

        // the minute value for each period must be greater than the previous period otherwise the schedule is invalid
        if (morningMin >= dayMin || dayMin >= eveningMin || eveningMin >= nightMin) {
            return null;
        }

        return "[" + morningMin + "," + morningTemp + "," + dayMin + "," + dayTemp + "," + eveningMin + ","
                + eveningTemp + "," + nightMin + "," + nightTemp + "]";
    }

    private int parseMinutes(String timeStr) {
        final String[] hourMin = timeStr.split(":");

        return Integer.parseInt(hourMin[0]) * 60 + Integer.parseInt(hourMin[1]);
    }
}
