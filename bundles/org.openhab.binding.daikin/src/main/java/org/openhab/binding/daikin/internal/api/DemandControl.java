/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.daikin.internal.api;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.daikin.internal.api.Enums.DemandControlMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * Class for holding the set of parameters used by set and get demand control info.
 *
 * @author Jimmy Tanagra - Initial Contribution
 *
 */
@NonNullByDefault
public class DemandControl {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemandControl.class);

    private static final List<String> DAYS = List.of("monday", "tuesday", "wednesday", "thursday", "friday", "saturday",
            "sunday");
    // create a map of "monday" -> "mo", "tuesday" -> "tu", etc.
    private static final Map<String, String> DAYS_ABBREVIATIONS = DAYS.stream()
            .map(day -> Map.entry(day, day.substring(0, 2)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    private static final Gson GSON = new Gson();

    public String ret = "";

    public DemandControlMode mode = DemandControlMode.AUTO;
    public int maxPower = 100;
    private Map<String, List<ScheduleEntry>> scheduleMap = new HashMap<>();

    private DemandControl() {
    }

    public String getSchedule() {
        return GSON.toJson(scheduleMap);
    }

    public void setSchedule(String schedule) throws JsonSyntaxException {
        Map<String, List<ScheduleEntry>> parsedMap = GSON.fromJson(schedule,
                new TypeToken<Map<String, List<ScheduleEntry>>>() {
                }.getType());

        if (DAYS.containsAll(parsedMap.keySet())) {
            scheduleMap = parsedMap;
        } else {
            throw new JsonSyntaxException("Invalid day(s) in JSON data");
        }
    }

    public int getScheduledMaxPower() {
        return getScheduledMaxPower(LocalDateTime.now());
    }

    // Returns the current max_power setting based on the schedule
    // If there are no matching schedules for the current time,
    // it will search the last schedule of the previous non-empty day
    public int getScheduledMaxPower(LocalDateTime dateTime) {
        int todayIndex = dateTime.getDayOfWeek().getValue() - 1;
        String today = DAYS.get(todayIndex);
        int currentMinsFromMidnight = dateTime.toLocalTime().toSecondOfDay() / 60;

        // search today's schedule for the last applicable schedule
        Optional<Integer> maxPower = scheduleMap.get(today).stream().filter(entry -> entry.enabled)
                .sorted((s1, s2) -> Integer.compare(s1.time, s2.time))
                .takeWhile(scheduleEntry -> scheduleEntry.time <= currentMinsFromMidnight)
                .reduce((first, second) -> second) // get the last entry that matches the condition
                .map(scheduleEntry -> scheduleEntry.power).or(() -> {
                    // there are no matching schedules today, so
                    // get the last entry of the previous non-empty schedule day,
                    // wrapping around the DAYS array if necessary

                    int currentIndex = todayIndex > 0 ? (todayIndex - 1) : (DAYS.size() - 1);
                    while (currentIndex != todayIndex) {
                        String prevDay = DAYS.get(currentIndex);
                        List<ScheduleEntry> prevDaySchedules = scheduleMap.get(prevDay).stream()
                                .filter(entry -> entry.enabled).sorted((s1, s2) -> Integer.compare(s1.time, s2.time))
                                .toList();
                        if (!prevDaySchedules.isEmpty()) {
                            return Optional.of(prevDaySchedules.get(prevDaySchedules.size() - 1).power);
                        }
                        currentIndex = currentIndex > 0 ? (currentIndex - 1) : (DAYS.size() - 1);
                    }

                    // if previous days have no schedules, use today's last schedule if any
                    return scheduleMap.get(today).stream().filter(entry -> entry.enabled)
                            .sorted((s1, s2) -> Integer.compare(s1.time, s2.time)).reduce((first, second) -> second)
                            .map(scheduleEntry -> scheduleEntry.power);
                });

        return maxPower.map(value -> value == 0 ? 100 : value) // a maxPower of 0 means the demand control is disabled,
                                                               // so return 100
                .orElse(100); // return 100 also for no schedules
    }

    public static DemandControl parse(String response) {
        LOGGER.trace("Parsing string: \"{}\"", response);

        Map<String, String> responseMap = InfoParser.parse(response);

        DemandControl info = new DemandControl();
        info.ret = responseMap.getOrDefault("ret", "");
        boolean enabled = "1".equals(responseMap.get("en_demand"));
        if (!enabled) {
            info.mode = DemandControlMode.OFF;
        } else {
            info.mode = DemandControlMode.fromValue(responseMap.getOrDefault("mode", "-"));
        }
        info.maxPower = Objects.requireNonNull(Optional.ofNullable(responseMap.get("max_pow"))
                .flatMap(value -> InfoParser.parseInt(value)).orElse(100));

        info.scheduleMap = DAYS_ABBREVIATIONS.entrySet().stream().map(day -> {
            final String dayName = day.getKey();
            final String dayPrefix = day.getValue();

            final int dayCount = Objects.requireNonNull(Optional.ofNullable(responseMap.get(dayPrefix + "c"))
                    .flatMap(value -> InfoParser.parseInt(value)).orElse(0));

            // We don't want to sort the entries by time here, to preserve the same order from the response
            List<ScheduleEntry> schedules = Stream.iterate(1, i -> i <= dayCount, i -> i + 1).map(i -> {
                String prefix = dayPrefix + i + "_";
                return new ScheduleEntry("1".equals(responseMap.get(prefix + "en")),
                        Objects.requireNonNull(Optional.ofNullable(responseMap.get(prefix + "t"))
                                .flatMap(value -> InfoParser.parseInt(value)).orElse(0)),
                        Objects.requireNonNull(Optional.ofNullable(responseMap.get(prefix + "p"))
                                .flatMap(value -> InfoParser.parseInt(value)).orElse(0)));
            }).toList();

            return Map.entry(dayName, schedules);
        }).collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

        return info;
    }

    public Map<String, String> getParamString() {
        Map<String, String> params = new HashMap<>();
        params.put("en_demand", mode == DemandControlMode.OFF ? "0" : "1");
        if (mode != DemandControlMode.OFF) {
            params.put("mode", mode.getValue());
            params.put("max_pow", Integer.toString(maxPower));
            DAYS.stream().forEach(day -> {
                String dayPrefix = DAYS_ABBREVIATIONS.get(day);
                List<ScheduleEntry> schedules = scheduleMap.getOrDefault(day, List.of());
                params.put(dayPrefix + "c", Integer.toString(schedules.size()));
                for (int i = 0; i < schedules.size(); i++) {
                    ScheduleEntry schedule = schedules.get(i);
                    String prefix = dayPrefix + (i + 1) + "_";
                    params.put(prefix + "en", schedule.enabled ? "1" : "0");
                    params.put(prefix + "t", Integer.toString(schedule.time));
                    params.put(prefix + "p", Integer.toString(schedule.power));
                }
            });
        }

        return params;
    }

    // package private for testing
    record ScheduleEntry(boolean enabled, int time, int power) {
    }
}
