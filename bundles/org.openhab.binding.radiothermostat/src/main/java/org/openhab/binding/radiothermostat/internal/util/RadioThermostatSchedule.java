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
package org.openhab.binding.radiothermostat.internal.util;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.radiothermostat.internal.RadioThermostatConfiguration;
import org.openhab.binding.radiothermostat.internal.dto.RadioThermostatTstatDTO;

/**
 * The {@link RadioThermostatSchedule} is the class used to convert the heating and cooling schedules from user
 * configuration into the json that is sent to the thermostat
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class RadioThermostatSchedule {

    private final ArrayList<DaySchedule> heatSchedule = new ArrayList<DaySchedule>();
    private final ArrayList<DaySchedule> coolSchedule = new ArrayList<DaySchedule>();

    public RadioThermostatSchedule(RadioThermostatConfiguration config) {
        heatSchedule.add(new DaySchedule(
                new ArrayList<SetPeriod>(List.of(new SetPeriod(config.monMorningHeatTime, config.monMorningHeatTemp),
                        new SetPeriod(config.monDayHeatTime, config.monDayHeatTemp),
                        new SetPeriod(config.monEveningHeatTime, config.monEveningHeatTemp),
                        new SetPeriod(config.monNightHeatTime, config.monNightHeatTemp)))));

        heatSchedule.add(new DaySchedule(
                new ArrayList<SetPeriod>(List.of(new SetPeriod(config.tueMorningHeatTime, config.tueMorningHeatTemp),
                        new SetPeriod(config.tueDayHeatTime, config.tueDayHeatTemp),
                        new SetPeriod(config.tueEveningHeatTime, config.tueEveningHeatTemp),
                        new SetPeriod(config.tueNightHeatTime, config.tueNightHeatTemp)))));

        heatSchedule.add(new DaySchedule(
                new ArrayList<SetPeriod>(List.of(new SetPeriod(config.wedMorningHeatTime, config.wedMorningHeatTemp),
                        new SetPeriod(config.wedDayHeatTime, config.wedDayHeatTemp),
                        new SetPeriod(config.wedEveningHeatTime, config.wedEveningHeatTemp),
                        new SetPeriod(config.wedNightHeatTime, config.wedNightHeatTemp)))));

        heatSchedule.add(new DaySchedule(
                new ArrayList<SetPeriod>(List.of(new SetPeriod(config.thuMorningHeatTime, config.thuMorningHeatTemp),
                        new SetPeriod(config.thuDayHeatTime, config.thuDayHeatTemp),
                        new SetPeriod(config.thuEveningHeatTime, config.thuEveningHeatTemp),
                        new SetPeriod(config.thuNightHeatTime, config.thuNightHeatTemp)))));

        heatSchedule.add(new DaySchedule(
                new ArrayList<SetPeriod>(List.of(new SetPeriod(config.friMorningHeatTime, config.friMorningHeatTemp),
                        new SetPeriod(config.friDayHeatTime, config.friDayHeatTemp),
                        new SetPeriod(config.friEveningHeatTime, config.friEveningHeatTemp),
                        new SetPeriod(config.friNightHeatTime, config.friNightHeatTemp)))));

        heatSchedule.add(new DaySchedule(
                new ArrayList<SetPeriod>(List.of(new SetPeriod(config.satMorningHeatTime, config.satMorningHeatTemp),
                        new SetPeriod(config.satDayHeatTime, config.satDayHeatTemp),
                        new SetPeriod(config.satEveningHeatTime, config.satEveningHeatTemp),
                        new SetPeriod(config.satNightHeatTime, config.satNightHeatTemp)))));

        heatSchedule.add(new DaySchedule(
                new ArrayList<SetPeriod>(List.of(new SetPeriod(config.sunMorningHeatTime, config.sunMorningHeatTemp),
                        new SetPeriod(config.sunDayHeatTime, config.sunDayHeatTemp),
                        new SetPeriod(config.sunEveningHeatTime, config.sunEveningHeatTemp),
                        new SetPeriod(config.sunNightHeatTime, config.sunNightHeatTemp)))));

        coolSchedule.add(new DaySchedule(
                new ArrayList<SetPeriod>(List.of(new SetPeriod(config.monMorningCoolTime, config.monMorningCoolTemp),
                        new SetPeriod(config.monDayCoolTime, config.monDayCoolTemp),
                        new SetPeriod(config.monEveningCoolTime, config.monEveningCoolTemp),
                        new SetPeriod(config.monNightCoolTime, config.monNightCoolTemp)))));

        coolSchedule.add(new DaySchedule(
                new ArrayList<SetPeriod>(List.of(new SetPeriod(config.tueMorningCoolTime, config.tueMorningCoolTemp),
                        new SetPeriod(config.tueDayCoolTime, config.tueDayCoolTemp),
                        new SetPeriod(config.tueEveningCoolTime, config.tueEveningCoolTemp),
                        new SetPeriod(config.tueNightCoolTime, config.tueNightCoolTemp)))));

        coolSchedule.add(new DaySchedule(
                new ArrayList<SetPeriod>(List.of(new SetPeriod(config.wedMorningCoolTime, config.wedMorningCoolTemp),
                        new SetPeriod(config.wedDayCoolTime, config.wedDayCoolTemp),
                        new SetPeriod(config.wedEveningCoolTime, config.wedEveningCoolTemp),
                        new SetPeriod(config.wedNightCoolTime, config.wedNightCoolTemp)))));

        coolSchedule.add(new DaySchedule(
                new ArrayList<SetPeriod>(List.of(new SetPeriod(config.thuMorningCoolTime, config.thuMorningCoolTemp),
                        new SetPeriod(config.thuDayCoolTime, config.thuDayCoolTemp),
                        new SetPeriod(config.thuEveningCoolTime, config.thuEveningCoolTemp),
                        new SetPeriod(config.thuNightCoolTime, config.thuNightCoolTemp)))));

        coolSchedule.add(new DaySchedule(
                new ArrayList<SetPeriod>(List.of(new SetPeriod(config.friMorningCoolTime, config.friMorningCoolTemp),
                        new SetPeriod(config.friDayCoolTime, config.friDayCoolTemp),
                        new SetPeriod(config.friEveningCoolTime, config.friEveningCoolTemp),
                        new SetPeriod(config.friNightCoolTime, config.friNightCoolTemp)))));

        coolSchedule.add(new DaySchedule(
                new ArrayList<SetPeriod>(List.of(new SetPeriod(config.satMorningCoolTime, config.satMorningCoolTemp),
                        new SetPeriod(config.satDayCoolTime, config.satDayCoolTemp),
                        new SetPeriod(config.satEveningCoolTime, config.satEveningCoolTemp),
                        new SetPeriod(config.satNightCoolTime, config.satNightCoolTemp)))));

        coolSchedule.add(new DaySchedule(
                new ArrayList<SetPeriod>(List.of(new SetPeriod(config.sunMorningCoolTime, config.sunMorningCoolTemp),
                        new SetPeriod(config.sunDayCoolTime, config.sunDayCoolTemp),
                        new SetPeriod(config.sunEveningCoolTime, config.sunEveningCoolTemp),
                        new SetPeriod(config.sunNightCoolTime, config.sunNightCoolTemp)))));
    }

    public String getHeatProgramJson() throws IllegalStateException {
        return getProgramJson(heatSchedule);
    }

    public String getCoolProgramJson() throws IllegalStateException {
        return getProgramJson(coolSchedule);
    }

    private String getProgramJson(ArrayList<DaySchedule> schedule) throws IllegalStateException {
        // all were null, bypass
        if (schedule.stream().allMatch(day -> day.isAnyNull())) {
            return "";
        }

        // some were null, the schedule is invalid
        if (schedule.stream().anyMatch(day -> day.isAnyNull())) {
            throw new IllegalStateException();
        }

        final StringBuilder json = new StringBuilder("{");
        IntStream.range(0, 7).forEach(i -> {
            json.append("\"" + i + "\":" + getDaySchedule(schedule.get(i)));
            if (i < 6) {
                json.append(",");
            }
        });
        json.append("}");

        return json.toString();
    }

    private @Nullable String getDaySchedule(DaySchedule day) {
        // if any of the time or temp fields are null, this day schedule is not valid
        if (day.isAnyNull()) {
            return null;
        }

        final ArrayList<SetPeriod> setPeriods = day.getSchedule();

        final int morningMin;
        final int dayMin;
        final int eveningMin;
        final int nightMin;

        try {
            morningMin = setPeriods.get(0).getMinutes();
            dayMin = setPeriods.get(1).getMinutes();
            eveningMin = setPeriods.get(2).getMinutes();
            nightMin = setPeriods.get(3).getMinutes();
        } catch (NumberFormatException nfe) {
            // if any of the times could not be parsed into minutes, the schedule is invalid
            return null;
        }

        // the minute value for each period must be greater than the previous period otherwise the schedule is invalid
        if (morningMin >= dayMin || dayMin >= eveningMin || eveningMin >= nightMin) {
            return null;
        }

        return "[" + morningMin + "," + setPeriods.get(0).getTemp() + "," + dayMin + "," + setPeriods.get(1).getTemp()
                + "," + eveningMin + "," + setPeriods.get(2).getTemp() + "," + nightMin + ","
                + setPeriods.get(3).getTemp() + "]";
    }

    public class DaySchedule {
        private final ArrayList<SetPeriod> schedule;

        public DaySchedule(ArrayList<SetPeriod> schedule) {
            this.schedule = schedule;
        }

        public ArrayList<SetPeriod> getSchedule() {
            return schedule;
        }

        public boolean isAnyNull() {
            return schedule.stream().anyMatch(itm -> itm.getTime() == null || itm.getTemp() == null);
        }
    }

    public class SetPeriod {
        private final @Nullable String time;
        private final @Nullable Integer temp;

        public SetPeriod(@Nullable String time, @Nullable Integer temp) {
            this.time = time;
            this.temp = temp;
        }

        public @Nullable ZonedDateTime getTime() {
            final String timeLocal = time;

            if (timeLocal != null) {
                final String[] hourMin = timeLocal.split(":");

                // get a zdt with the hour and minute of the next set point
                final ZonedDateTime nextSetTime = ZonedDateTime.now().withHour(Integer.parseInt(hourMin[0]))
                        .withMinute(Integer.parseInt(hourMin[1])).withSecond(0).withNano(0);

                // if the next set point occurs tomorrow, add one day to the zdt
                if (nextSetTime.isBefore(ZonedDateTime.now())) {
                    return nextSetTime.plusDays(1);
                }
                return nextSetTime;

            }
            return null;
        }

        public @Nullable Integer getTemp() {
            return temp;
        }

        public int getMinutes() {
            final String timeLocal = time;
            final String[] hourMin = timeLocal != null ? timeLocal.split(":") : new String[] { "" };
            return Integer.parseInt(hourMin[0]) * 60 + Integer.parseInt(hourMin[1]);
        }
    }

    public @Nullable Integer getNextTemp(RadioThermostatTstatDTO thermostatData) {
        final SetPeriod nextPeriod = getNextSetpoint(thermostatData);
        return nextPeriod != null ? nextPeriod.getTemp() : null;
    }

    public @Nullable ZonedDateTime getNextTime(RadioThermostatTstatDTO thermostatData) {
        final SetPeriod nextPeriod = getNextSetpoint(thermostatData);
        return nextPeriod != null ? nextPeriod.getTime() : null;
    }

    private @Nullable SetPeriod getNextSetpoint(RadioThermostatTstatDTO thermostatData) {
        if (thermostatData.getHold().equals(1)) {
            return null;
        }

        final ArrayList<DaySchedule> schedule;

        if (thermostatData.getMode().equals(1)) {
            schedule = heatSchedule;
        } else if (thermostatData.getMode().equals(2)) {
            schedule = coolSchedule;
        } else {
            return null;
        }

        final DaySchedule daySched = schedule.get(thermostatData.getTime().getDayOfWeek().intValue());
        int nextPeriod = 0;

        try {
            for (int i = 0; i <= 3; i++) {
                if (thermostatData.getTime().getRuntime().intValue() >= daySched.getSchedule().get(i).getMinutes()) {
                    if (i == 3) {
                        nextPeriod = -1;
                        break;
                    } else {
                        nextPeriod = i + 1;
                    }
                }
            }
        } catch (NumberFormatException e) {
            return null;
        }

        if (nextPeriod == -1) {
            int nextDay = thermostatData.getTime().getDayOfWeek().intValue() + 1;
            if (nextDay == 7) {
                nextDay = 0;
            }

            return schedule.get(nextDay).getSchedule().get(0);
        }
        return daySched.getSchedule().get(nextPeriod);
    }
}
