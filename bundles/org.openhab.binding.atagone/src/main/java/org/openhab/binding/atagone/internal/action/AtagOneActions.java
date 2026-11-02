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
package org.openhab.binding.atagone.internal.action;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atagone.internal.AtagOneHandler;
import org.openhab.core.automation.Visibility;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thing Actions for the ATAG ONE binding — single-call mode activation and schedule editing.
 *
 * @author Florian Lettner - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = AtagOneActions.class)
@ThingActionsScope(name = "atagone")
@NonNullByDefault
public class AtagOneActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(AtagOneActions.class);

    private @Nullable AtagOneHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof AtagOneHandler atagOneHandler) {
            this.handler = atagOneHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/action.activate-vacation.label", description = "@text/action.activate-vacation.description")
    public void activateVacation(
            @ActionInput(name = "days", label = "@text/action.activate-vacation.input.days.label", description = "@text/action.activate-vacation.input.days.description") int days) {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("activateVacation called with no handler bound");
            return;
        }
        if (days <= 0) {
            logger.warn("activateVacation: days must be positive, got {}", days);
            return;
        }
        theHandler.enqueueVacationActivation(days * AtagOneHandler.SECONDS_PER_DAY);
    }

    @RuleAction(label = "@text/action.activate-extend.label", description = "@text/action.activate-extend.description")
    public void activateExtend(
            @ActionInput(name = "minutes", label = "@text/action.activate-extend.input.minutes.label", description = "@text/action.activate-extend.input.minutes.description") int minutes) {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("activateExtend called with no handler bound");
            return;
        }
        if (minutes <= 0 || minutes % 15 != 0) {
            logger.warn("activateExtend: minutes must be a positive multiple of 15, got {}", minutes);
            return;
        }
        theHandler.enqueueExtendActivation(minutes * 60L);
    }

    @RuleAction(label = "@text/action.activate-fireplace.label", description = "@text/action.activate-fireplace.description")
    public void activateFireplace(
            @ActionInput(name = "hours", label = "@text/action.activate-fireplace.input.hours.label", description = "@text/action.activate-fireplace.input.hours.description") int hours) {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("activateFireplace called with no handler bound");
            return;
        }
        if (hours <= 0) {
            logger.warn("activateFireplace: hours must be positive, got {}", hours);
            return;
        }
        theHandler.enqueueFireplaceActivation(hours * AtagOneHandler.SECONDS_PER_HOUR);
    }

    @RuleAction(label = "@text/action.set-ch-schedule-period.label", description = "@text/action.set-ch-schedule-period.description")
    public boolean setChSchedulePeriod(
            @ActionInput(name = "weekday", label = "@text/action.set-ch-schedule-period.input.weekday.label", description = "@text/action.set-ch-schedule-period.input.weekday.description") String weekday,
            @ActionInput(name = "start", label = "@text/action.set-ch-schedule-period.input.start.label", description = "@text/action.set-ch-schedule-period.input.start.description") LocalTime start,
            @ActionInput(name = "end", label = "@text/action.set-ch-schedule-period.input.end.label", description = "@text/action.set-ch-schedule-period.input.end.description") LocalTime end,
            @ActionInput(name = "temperatureCelsius", label = "@text/action.set-ch-schedule-period.input.temperatureCelsius.label", description = "@text/action.set-ch-schedule-period.input.temperatureCelsius.description") double temperatureCelsius) {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("setChSchedulePeriod called with no handler bound");
            return false;
        }
        boolean accepted = theHandler.enqueueChSchedulePeriodSet(weekday, minutesSinceMidnight(start),
                endMinutesSinceMidnight(end), temperatureCelsius);
        if (!accepted) {
            logger.warn(
                    "setChSchedulePeriod: invalid weekday or time range, an overlap that cannot be trimmed, no CH schedule polled yet, or Thing not ONLINE");
        }
        return accepted;
    }

    public boolean setChSchedulePeriod(String weekday, String start, String end, double temperatureCelsius) {
        try {
            return setChSchedulePeriod(weekday, LocalTime.parse(start), LocalTime.parse(end), temperatureCelsius);
        } catch (DateTimeParseException e) {
            logger.warn("setChSchedulePeriod: {}", e.getMessage());
            return false;
        }
    }

    @RuleAction(label = "@text/action.clear-ch-schedule-period.label", description = "@text/action.clear-ch-schedule-period.description")
    public boolean clearChSchedulePeriod(
            @ActionInput(name = "weekday", label = "@text/action.clear-ch-schedule-period.input.weekday.label", description = "@text/action.clear-ch-schedule-period.input.weekday.description") String weekday,
            @ActionInput(name = "start", label = "@text/action.clear-ch-schedule-period.input.start.label", description = "@text/action.clear-ch-schedule-period.input.start.description") LocalTime start) {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("clearChSchedulePeriod called with no handler bound");
            return false;
        }
        boolean accepted = theHandler.enqueueChSchedulePeriodClear(weekday, minutesSinceMidnight(start));
        if (!accepted) {
            logger.warn(
                    "clearChSchedulePeriod: invalid weekday, no period starting at that time, no CH schedule polled yet, or Thing not ONLINE");
        }
        return accepted;
    }

    public boolean clearChSchedulePeriod(String weekday, String start) {
        try {
            return clearChSchedulePeriod(weekday, LocalTime.parse(start));
        } catch (DateTimeParseException e) {
            logger.warn("clearChSchedulePeriod: {}", e.getMessage());
            return false;
        }
    }

    @RuleAction(label = "@text/action.set-dhw-schedule-period.label", description = "@text/action.set-dhw-schedule-period.description")
    public boolean setDhwSchedulePeriod(
            @ActionInput(name = "weekday", label = "@text/action.set-dhw-schedule-period.input.weekday.label", description = "@text/action.set-dhw-schedule-period.input.weekday.description") String weekday,
            @ActionInput(name = "start", label = "@text/action.set-dhw-schedule-period.input.start.label", description = "@text/action.set-dhw-schedule-period.input.start.description") LocalTime start,
            @ActionInput(name = "end", label = "@text/action.set-dhw-schedule-period.input.end.label", description = "@text/action.set-dhw-schedule-period.input.end.description") LocalTime end,
            @ActionInput(name = "temperatureCelsius", label = "@text/action.set-dhw-schedule-period.input.temperatureCelsius.label", description = "@text/action.set-dhw-schedule-period.input.temperatureCelsius.description") double temperatureCelsius) {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("setDhwSchedulePeriod called with no handler bound");
            return false;
        }
        boolean accepted = theHandler.enqueueDhwSchedulePeriodSet(weekday, minutesSinceMidnight(start),
                endMinutesSinceMidnight(end), temperatureCelsius);
        if (!accepted) {
            logger.warn(
                    "setDhwSchedulePeriod: invalid weekday or time range, an overlap that cannot be trimmed, no DHW schedule polled yet, or Thing not ONLINE");
        }
        return accepted;
    }

    public boolean setDhwSchedulePeriod(String weekday, String start, String end, double temperatureCelsius) {
        try {
            return setDhwSchedulePeriod(weekday, LocalTime.parse(start), LocalTime.parse(end), temperatureCelsius);
        } catch (DateTimeParseException e) {
            logger.warn("setDhwSchedulePeriod: {}", e.getMessage());
            return false;
        }
    }

    @RuleAction(label = "@text/action.clear-dhw-schedule-period.label", description = "@text/action.clear-dhw-schedule-period.description")
    public boolean clearDhwSchedulePeriod(
            @ActionInput(name = "weekday", label = "@text/action.clear-dhw-schedule-period.input.weekday.label", description = "@text/action.clear-dhw-schedule-period.input.weekday.description") String weekday,
            @ActionInput(name = "start", label = "@text/action.clear-dhw-schedule-period.input.start.label", description = "@text/action.clear-dhw-schedule-period.input.start.description") LocalTime start) {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("clearDhwSchedulePeriod called with no handler bound");
            return false;
        }
        boolean accepted = theHandler.enqueueDhwSchedulePeriodClear(weekday, minutesSinceMidnight(start));
        if (!accepted) {
            logger.warn(
                    "clearDhwSchedulePeriod: invalid weekday, no period starting at that time, no DHW schedule polled yet, or Thing not ONLINE");
        }
        return accepted;
    }

    public boolean clearDhwSchedulePeriod(String weekday, String start) {
        try {
            return clearDhwSchedulePeriod(weekday, LocalTime.parse(start));
        } catch (DateTimeParseException e) {
            logger.warn("clearDhwSchedulePeriod: {}", e.getMessage());
            return false;
        }
    }

    @RuleAction(label = "@text/action.set-ch-schedule.label", description = "@text/action.set-ch-schedule.description", visibility = Visibility.EXPERT)
    public boolean setChSchedule(
            @ActionInput(name = "json", label = "@text/action.set-ch-schedule.input.json.label", description = "@text/action.set-ch-schedule.input.json.description") String json) {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("setChSchedule called with no handler bound");
            return false;
        }
        boolean accepted = theHandler.enqueueChScheduleFromJson(json);
        if (!accepted) {
            logger.warn(
                    "setChSchedule: malformed JSON, unknown weekday, no CH schedule polled yet, or Thing not ONLINE");
        }
        return accepted;
    }

    @RuleAction(label = "@text/action.set-dhw-schedule.label", description = "@text/action.set-dhw-schedule.description", visibility = Visibility.EXPERT)
    public boolean setDhwSchedule(
            @ActionInput(name = "json", label = "@text/action.set-dhw-schedule.input.json.label", description = "@text/action.set-dhw-schedule.input.json.description") String json) {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("setDhwSchedule called with no handler bound");
            return false;
        }
        boolean accepted = theHandler.enqueueDhwScheduleFromJson(json);
        if (!accepted) {
            logger.warn(
                    "setDhwSchedule: malformed JSON, unknown weekday, no DHW schedule polled yet, or Thing not ONLINE");
        }
        return accepted;
    }

    @RuleAction(label = "@text/action.cancel-mode.label", description = "@text/action.cancel-mode.description")
    public @ActionOutput(type = "java.lang.Boolean", label = "@text/action.cancel-mode.output.label", description = "@text/action.cancel-mode.output.description") boolean cancelMode() {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("cancelMode called with no handler bound");
            return false;
        }
        return theHandler.enqueueCancel();
    }

    private static int minutesSinceMidnight(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    private static int endMinutesSinceMidnight(LocalTime time) {
        return LocalTime.MIDNIGHT.equals(time) ? 24 * 60 : minutesSinceMidnight(time);
    }

    public static void activateVacation(ThingActions actions, int days) {
        ((AtagOneActions) actions).activateVacation(days);
    }

    public static void activateExtend(ThingActions actions, int minutes) {
        ((AtagOneActions) actions).activateExtend(minutes);
    }

    public static void activateFireplace(ThingActions actions, int hours) {
        ((AtagOneActions) actions).activateFireplace(hours);
    }

    public static boolean cancelMode(ThingActions actions) {
        return ((AtagOneActions) actions).cancelMode();
    }

    public static boolean setChSchedulePeriod(ThingActions actions, String weekday, LocalTime start, LocalTime end,
            double temperatureCelsius) {
        return ((AtagOneActions) actions).setChSchedulePeriod(weekday, start, end, temperatureCelsius);
    }

    public static boolean setChSchedulePeriod(ThingActions actions, String weekday, String start, String end,
            double temperatureCelsius) {
        return ((AtagOneActions) actions).setChSchedulePeriod(weekday, start, end, temperatureCelsius);
    }

    public static boolean clearChSchedulePeriod(ThingActions actions, String weekday, LocalTime start) {
        return ((AtagOneActions) actions).clearChSchedulePeriod(weekday, start);
    }

    public static boolean clearChSchedulePeriod(ThingActions actions, String weekday, String start) {
        return ((AtagOneActions) actions).clearChSchedulePeriod(weekday, start);
    }

    public static boolean setDhwSchedulePeriod(ThingActions actions, String weekday, LocalTime start, LocalTime end,
            double temperatureCelsius) {
        return ((AtagOneActions) actions).setDhwSchedulePeriod(weekday, start, end, temperatureCelsius);
    }

    public static boolean setDhwSchedulePeriod(ThingActions actions, String weekday, String start, String end,
            double temperatureCelsius) {
        return ((AtagOneActions) actions).setDhwSchedulePeriod(weekday, start, end, temperatureCelsius);
    }

    public static boolean clearDhwSchedulePeriod(ThingActions actions, String weekday, LocalTime start) {
        return ((AtagOneActions) actions).clearDhwSchedulePeriod(weekday, start);
    }

    public static boolean clearDhwSchedulePeriod(ThingActions actions, String weekday, String start) {
        return ((AtagOneActions) actions).clearDhwSchedulePeriod(weekday, start);
    }

    public static boolean setChSchedule(ThingActions actions, String json) {
        return ((AtagOneActions) actions).setChSchedule(json);
    }

    public static boolean setDhwSchedule(ThingActions actions, String json) {
        return ((AtagOneActions) actions).setDhwSchedule(json);
    }
}
