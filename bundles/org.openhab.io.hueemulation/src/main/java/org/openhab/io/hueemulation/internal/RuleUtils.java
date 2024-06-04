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
package org.openhab.io.hueemulation.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.util.ModuleBuilder;
import org.openhab.core.config.core.Configuration;
import org.openhab.io.hueemulation.internal.dto.HueDataStore;
import org.openhab.io.hueemulation.internal.dto.HueGroupEntry;
import org.openhab.io.hueemulation.internal.dto.HueLightEntry;
import org.openhab.io.hueemulation.internal.dto.changerequest.HueCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rule utility methods. The Hue scheduler and Hue rules support is based on the automation engine.
 * This class provides methods to convert between Hue entries and automation rules.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class RuleUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuleUtils.class);
    public static Random random = new Random(); // public for test mock

    /**
     * Splits the given base time (pattern "hh:mm[:ss]") on the colons and return the resulting array.
     * If an upper time is given (same pattern), a random number between base and upper time is chosen.
     *
     * @param baseTime A base time with the pattern hh:mm[:ss]
     * @param upperTime An optional upper time or null or an empty string
     * @return Time components (hour, minute, seconds).
     */
    public static String[] computeRandomizedDayTime(String baseTime, @Nullable String upperTime) {
        if (!baseTime.matches("[0-9]{1,2}:[0-9]{1,2}(:[0-9]{1,2})?")) {
            throw new IllegalStateException("Time pattern incorrect. Must be 'hh:mm[:ss]'. " + baseTime);
        }

        String[] randomizedTime = baseTime.split(":");

        if (upperTime != null && !upperTime.isEmpty()) {
            String[] upperTimeParts = upperTime.split(":");
            if (!upperTime.matches("[0-9]{1,2}:[0-9]{1,2}(:[0-9]{1,2})?")
                    || randomizedTime.length != upperTimeParts.length) {
                throw new IllegalStateException("Random Time pattern incorrect. Must be 'hh:mm[:ss]'. " + upperTime);
            }
            for (int i = 0; i < randomizedTime.length; ++i) {
                int n = Integer.parseInt(randomizedTime[i]);
                int n2 = Integer.parseInt(upperTimeParts[i]);
                int diff = Math.abs(n2 - n); // Example: 12 and 14 -> diff = 2
                if (diff > 0) { // diff = rnd [0,3)
                    diff = random.nextInt(diff + 1);
                }
                randomizedTime[i] = String.valueOf(n2 > n ? n + diff : n2 + diff);
            }
        }

        return randomizedTime;
    }

    /**
     * Validates a hue http address used in schedules and hue rules.
     *
     * @param ds A hue datastore to verify that referred lights/groups do exist
     * @param address Relative hue API address. Example: {@code "/api/<username>/groups/1/action"} or
     *            {@code "/api/<username>/lights/1/state"}
     * @throws IllegalStateException Thrown if address is invalid
     */
    @SuppressWarnings({ "unused", "null" })
    public static void validateHueHttpAddress(HueDataStore ds, String address) throws IllegalStateException {
        String[] validation = address.split("/");
        if (validation.length < 6 || !validation[0].isEmpty() || !"api".equals(validation[1])) {
            throw new IllegalStateException("Given address invalid!");
        }
        if ("groups".equals(validation[3]) && "action".equals(validation[5])) {
            HueGroupEntry entry = ds.groups.get(validation[4]);
            if (entry == null) {
                throw new IllegalStateException("Group does not exist: " + validation[4]);
            }
        } else if ("lights".equals(validation[3]) && "state".equals(validation[5])) {
            HueLightEntry entry = ds.lights.get(validation[4]);
            if (entry == null) {
                throw new IllegalStateException("Light does not exist: " + validation[4]);
            }
        } else {
            throw new IllegalStateException("Can only handle groups and lights");
        }
    }

    public static class ConfigHttpAction {
        public String url = "";
        public String method = "";
        public String body = "";
    }

    @SuppressWarnings({ "unused", "null" })
    public static @Nullable HueCommand httpActionToHueCommand(HueDataStore ds, Action a, @Nullable String ruleName) {
        ConfigHttpAction config = a.getConfiguration().as(ConfigHttpAction.class);

        // Example: "/api/<username>/groups/1/action" or "/api/<username>/lights/1/state"
        String[] validation = config.url.split("/");
        if (validation.length < 6 || !validation[0].isEmpty() || !"api".equals(validation[1])) {
            LOGGER.warn("Hue Rule '{}': Given address in action {} invalid!", ruleName, a.getLabel());
            return null;
        }

        if ("groups".equals(validation[3]) && "action".equals(validation[5])) {
            HueGroupEntry gentry = ds.groups.get(validation[4]);
            if (gentry == null) {
                LOGGER.warn("Hue Rule '{}': Given address in action {} invalid. Group does not exist: {}", ruleName,
                        a.getLabel(), validation[4]);
                return null;
            }
            return new HueCommand(config.url, config.method, config.body);
        } else if ("lights".equals(validation[3]) && "state".equals(validation[5])) {
            HueLightEntry lentry = ds.lights.get(validation[4]);
            if (lentry == null) {
                LOGGER.warn("Hue Rule '{}': Given address in action {} invalid. Light does not exist: {}", ruleName,
                        a.getLabel(), validation[4]);
                return null;
            }
            return new HueCommand(config.url, config.method, config.body);
        } else {
            LOGGER.warn("Hue Rule '{}': Given address in action {} invalid. Can only handle lights and groups, not {}",
                    ruleName, a.getLabel(), validation[3]);
            return null;
        }
    }

    public static Action createHttpAction(HueCommand command, String id) {
        final Configuration actionConfig = new Configuration();
        actionConfig.put("method", command.method);
        actionConfig.put("url", command.address);
        actionConfig.put("body", command.body);
        return ModuleBuilder.createAction().withId(id).withTypeUID("rules.HttpAction").withConfiguration(actionConfig)
                .build();
    }

    // Recurring pattern "W[bbb]/T[hh]:[mm]:[ss]A[hh]:[mm]:[ss]"
    private static Trigger createRecurringFromTimeString(String localtime) {
        Pattern timePattern = Pattern.compile("W(.*)/T(.*)A(.*)");
        Matcher m = timePattern.matcher(localtime);
        if (!m.matches() || m.groupCount() < 3) {
            throw new IllegalStateException("Recurring time pattern incorrect");
        }

        String weekdays = m.group(1);
        String time = m.group(2);
        String randomize = m.group(3);

        // Monday = 64, Tuesday = 32, Wednesday = 16, Thursday = 8, Friday = 4, Saturday = 2, Sunday = 1
        int weekdaysBinaryEncoded = Integer.valueOf(weekdays);
        // For the last part of the cron expression ("day"):
        // A comma separated list of days starting with 0=sunday to 7=sunday
        List<String> cronWeekdays = new ArrayList<>();
        for (int bin = 64, c = 1; bin > 0; bin /= 2, c += 1) {
            if (weekdaysBinaryEncoded / bin == 1) {
                weekdaysBinaryEncoded = weekdaysBinaryEncoded % bin;
                cronWeekdays.add(String.valueOf(c));
            }
        }
        String[] hourMinSec = RuleUtils.computeRandomizedDayTime(time, randomize);

        // Cron expression: min hour day month weekdays
        String cronExpression = hourMinSec[1] + " " + hourMinSec[0] + " * * " + String.join(",", cronWeekdays);

        final Configuration triggerConfig = new Configuration();
        triggerConfig.put("cronExpression", cronExpression);
        return ModuleBuilder.createTrigger().withId("crontrigger").withTypeUID("timer.GenericCronTrigger")
                .withConfiguration(triggerConfig).build();
    }

    // Timer pattern: R[nn]/PT[hh]:[mm]:[ss]A[hh]:[mm]:[ss]
    private static Trigger createTimerFromTimeString(String localtime) {
        Pattern timePattern = Pattern.compile("R(.*)/PT(.*)A(.*)");
        Matcher m = timePattern.matcher(localtime);
        if (!m.matches() || m.groupCount() < 3) {
            throw new IllegalStateException("Timer pattern incorrect");
        }

        String run = m.group(1);
        String time = m.group(2);
        String randomize = m.group(3);

        final Configuration triggerConfig = new Configuration();
        if (!time.matches("[0-9]{1,2}:[0-9]{1,2}(:[0-9]{1,2})?")) {
            throw new IllegalStateException("Time pattern incorrect. Must be 'hh:mm[:ss]'. " + time);
        }
        triggerConfig.put("time", time);
        if (randomize.matches("[0-9]{1,2}:[0-9]{1,2}(:[0-9]{1,2})?")) {
            triggerConfig.put("randomizeTime", randomize);
        }
        if (!run.isEmpty()) {
            if (!run.matches("[0-9]{1,2}")) {
                throw new IllegalStateException("Run pattern incorrent. Must be a number'. " + run);
            } else {
                triggerConfig.put("repeat", run);
            }
        } else { // Infinite
            triggerConfig.put("repeat", "-1");
        }

        return ModuleBuilder.createTrigger().withId("timertrigger").withTypeUID("timer.TimerTrigger")
                .withConfiguration(triggerConfig).build();
    }

    // Absolute date/time pattern "[YYYY]:[MM]:[DD]T[hh]:[mm]:[ss]A[hh]:[mm]:[ss]"
    private static Trigger createAbsoluteDateTimeFromTimeString(String localtime) {
        Pattern timePattern = Pattern.compile("(.*)T(.*)A(.*)");
        Matcher m = timePattern.matcher(localtime);
        if (!m.matches() || m.groupCount() < 3) {
            throw new IllegalStateException("Absolute date/time pattern incorrect");
        }

        String date = m.group(1);
        String time = m.group(2);
        if (!date.matches("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}")) {
            throw new IllegalStateException("Date pattern incorrect. Must be 'yyyy-mm-dd'. " + date);
        }
        if (!time.matches("[0-9]{1,2}:[0-9]{1,2}(:[0-9]{1,2})?")) {
            throw new IllegalStateException("Time pattern incorrect. Must be 'hh:mm[:ss]'. " + time);
        }
        final Configuration triggerConfig = new Configuration();

        triggerConfig.put("date", date);
        triggerConfig.put("time", time);

        time = m.group(3);
        if (time.matches("[0-9]{1,2}:[0-9]{1,2}(:[0-9]{1,2})?")) {
            triggerConfig.put("randomizeTime", time);
        }

        return ModuleBuilder.createTrigger().withId("absolutetrigger").withTypeUID("timer.AbsoluteDateTimeTrigger")
                .withConfiguration(triggerConfig).build();
    }

    /**
     * Creates a trigger based on the given time string.
     * According to <a href="https://developers.meethue.com/develop/hue-api/datatypes-and-time-patterns/">the Hue
     * documentation</a> this can be:
     * <p>
     * <ul>
     * <li>Absolute time [YYYY]-[MM]-[DD]T[hh]:[mm]:[ss] ([date]T[time])
     * <li>Randomized time [YYYY]:[MM]:[DD]T[hh]:[mm]:[ss]A[hh]:[mm]:[ss] ([date]T[time]A[time])
     * <li>Recurring times W[bbb]/T[hh]:[mm]:[ss]
     * <li>Every day of the week given by bbb at given time
     * <li>Recurring randomized times W[bbb]/T[hh]:[mm]:[ss]A[hh]:[mm]:[ss]
     * <li>Every weekday given by bbb at given left side time, randomized by right side time. Right side time has to be
     * smaller than 12 hours
     * <li>
     * Timers
     * <ul>
     * <li>PT[hh]:[mm]:[ss] Timer, expiring after given time
     * <li>PT[hh]:[mm]:[ss] Timer, expiring after given time
     * <li>PT[hh]:[mm]:[ss]A[hh]:[mm]:[ss] Timer with random element
     * <li>R[nn]/PT[hh]:[mm]:[ss] Recurring timer
     * <li>R/PT[hh]:[mm]:[ss] Recurring timer
     * <li>R[nn]/PT[hh]:[mm]:[ss]A[hh]:[mm]:[ss] Recurring timer with random element
     * </ul>
     * </ul>
     *
     * @param localtimeParameter An absolute time or recurring time or timer pattern
     * @return A trigger based on {@link org.openhab.io.hueemulation.internal.automation.AbsoluteDateTimeTriggerHandler}
     *         or {@link org.openhab.io.hueemulation.internal.automation.TimerTriggerHandler}
     */
    public static Trigger createTriggerForTimeString(final String localtimeParameter) throws IllegalStateException {
        String localtime = localtimeParameter;
        Trigger ruleTrigger;

        // Normalize timer patterns
        if (localtime.startsWith("PT")) {
            localtime = "R1/" + localtime;
        }
        if (!localtime.contains("A")) {
            localtime += "A";
        }

        // Recurring pattern "W[bbb]/T[hh]:[mm]:[ss]A[hh]:[mm]:[ss]"
        if (localtime.startsWith("W")) {
            ruleTrigger = createRecurringFromTimeString(localtime);
        } // Timer pattern: R[nn]/PT[hh]:[mm]:[ss]A[hh]:[mm]:[ss]
        else if (localtime.startsWith("R")) {
            ruleTrigger = createTimerFromTimeString(localtime);
        } // Absolute date/time pattern "[YYYY]:[MM]:[DD]T[hh]:[mm]:[ss]A[hh]:[mm]:[ss]"
        else {
            ruleTrigger = createAbsoluteDateTimeFromTimeString(localtime);
        }

        return ruleTrigger;
    }

    public static class TimerConfig {
        public String time = "";
        public String randomizeTime = "";
        public @Nullable Integer repeat;
    }

    public static @Nullable String timeStringFromTrigger(List<Trigger> triggers) {
        Optional<Trigger> trigger;

        trigger = triggers.stream().filter(p -> "crontrigger".equals(p.getId())).findFirst();
        if (trigger.isPresent()) {
            String[] cronParts = ((String) trigger.get().getConfiguration().get("cronExpression")).split(" ");
            if (cronParts.length != 5) {
                LOGGER.warn("Cron trigger has no valid cron expression: {}", String.join(",", cronParts));
                return null;
            }
            // Monday = 64, Tuesday = 32, Wednesday = 16, Thursday = 8, Friday = 4, Saturday = 2, Sunday = 1
            int weekdays = 0;
            String[] cronWeekdays = cronParts[4].split(",");
            for (String wdayT : cronWeekdays) {
                int wday = Integer.parseInt(wdayT);
                switch (wday) {
                    case 0:
                    case 7:
                        weekdays += 1;
                        break;
                    case 1:
                        weekdays += 64;
                        break;
                    case 2:
                        weekdays += 32;
                        break;
                    case 3:
                        weekdays += 16;
                        break;
                    case 4:
                        weekdays += 8;
                        break;
                    case 5:
                        weekdays += 4;
                        break;
                    case 6:
                        weekdays += 2;
                        break;
                }
            }
            return String.format("W%d/T%s:%s:00", weekdays, cronParts[1], cronParts[0]);
        }

        trigger = triggers.stream().filter(p -> "timertrigger".equals(p.getId())).findFirst();
        if (trigger.isPresent()) {
            TimerConfig c = trigger.get().getConfiguration().as(TimerConfig.class);
            if (c.repeat == null) {
                return String.format(c.randomizeTime.isEmpty() ? "PT%s" : "PT%sA%s", c.time, c.randomizeTime);
            } else if (c.repeat == -1) {
                return String.format(c.randomizeTime.isEmpty() ? "R/PT%s" : "R/PT%sA%s", c.time, c.randomizeTime);
            } else {
                return String.format(c.randomizeTime.isEmpty() ? "R%d/PT%s" : "R%d/PT%sA%s", c.repeat, c.time,
                        c.randomizeTime);
            }
        } else {
            trigger = triggers.stream().filter(p -> "absolutetrigger".equals(p.getId())).findFirst();
            if (trigger.isPresent()) {
                String date = (String) trigger.get().getConfiguration().get("date");
                String time = (String) trigger.get().getConfiguration().get("time");
                String randomizeTime = (String) trigger.get().getConfiguration().get("randomizeTime");
                return String.format(randomizeTime == null ? "%sT%s" : "%sT%sA%s", date, time, randomizeTime);
            } else {
                LOGGER.warn("No recognised trigger type");
                return null;
            }
        }
    }
}
