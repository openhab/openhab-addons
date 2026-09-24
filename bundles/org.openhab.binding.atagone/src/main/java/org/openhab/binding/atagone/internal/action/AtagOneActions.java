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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atagone.internal.AtagOneHandler;
import org.openhab.binding.atagone.internal.dto.ControlUpdateDTO;
import org.openhab.binding.atagone.internal.dto.DeviceConfigUpdateDTO;
import org.openhab.binding.atagone.internal.dto.ScheduleDTO;
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
            @ActionInput(name = "durationSeconds", label = "@text/action.activate-vacation.input.durationSeconds.label", description = "@text/action.activate-vacation.input.durationSeconds.description") long durationSeconds) {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("activateVacation called with no handler bound");
            return;
        }
        if (durationSeconds <= 0) {
            logger.warn("activateVacation: duration must be positive, got {}", durationSeconds);
            return;
        }
        if (!AtagOneHandler.isWholeUnits(durationSeconds, AtagOneHandler.SECONDS_PER_DAY)) {
            logger.warn("activateVacation: duration must be a whole number of days, got {} s", durationSeconds);
            return;
        }
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();
        theHandler.composeVacationActivation(control, configUpdate, durationSeconds);
        theHandler.sendComposedUpdate("action:activateVacation", control, configUpdate);
    }

    @RuleAction(label = "@text/action.activate-extend.label", description = "@text/action.activate-extend.description")
    public void activateExtend(
            @ActionInput(name = "durationSeconds", label = "@text/action.activate-extend.input.durationSeconds.label", description = "@text/action.activate-extend.input.durationSeconds.description") long durationSeconds) {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("activateExtend called with no handler bound");
            return;
        }
        if (durationSeconds <= 0) {
            logger.warn("activateExtend: duration must be positive, got {}", durationSeconds);
            return;
        }
        if (!AtagOneHandler.isWholeUnits(durationSeconds, AtagOneHandler.SECONDS_PER_15_MINUTES)) {
            logger.warn("activateExtend: duration must be a whole number of 15-minute increments, got {} s",
                    durationSeconds);
            return;
        }
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();
        theHandler.composeExtendActivation(control, durationSeconds);
        theHandler.sendComposedUpdate("action:activateExtend", control, configUpdate);
    }

    @RuleAction(label = "@text/action.activate-fireplace.label", description = "@text/action.activate-fireplace.description")
    public void activateFireplace(
            @ActionInput(name = "durationSeconds", label = "@text/action.activate-fireplace.input.durationSeconds.label", description = "@text/action.activate-fireplace.input.durationSeconds.description") long durationSeconds) {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("activateFireplace called with no handler bound");
            return;
        }
        if (durationSeconds <= 0) {
            logger.warn("activateFireplace: duration must be positive, got {}", durationSeconds);
            return;
        }
        if (!AtagOneHandler.isWholeUnits(durationSeconds, AtagOneHandler.SECONDS_PER_HOUR)) {
            logger.warn("activateFireplace: duration must be a whole number of hours, got {} s", durationSeconds);
            return;
        }
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();
        theHandler.composeFireplaceActivation(control, durationSeconds);
        theHandler.sendComposedUpdate("action:activateFireplace", control, configUpdate);
    }

    @RuleAction(label = "@text/action.set-ch-schedule-period.label", description = "@text/action.set-ch-schedule-period.description")
    public boolean setChSchedulePeriod(
            @ActionInput(name = "weekday", label = "@text/action.set-ch-schedule-period.input.weekday.label", description = "@text/action.set-ch-schedule-period.input.weekday.description") String weekday,
            @ActionInput(name = "periodIndex", label = "@text/action.set-ch-schedule-period.input.periodIndex.label", description = "@text/action.set-ch-schedule-period.input.periodIndex.description") int periodIndex,
            @ActionInput(name = "startMinutes", label = "@text/action.set-ch-schedule-period.input.startMinutes.label", description = "@text/action.set-ch-schedule-period.input.startMinutes.description") int startMinutes,
            @ActionInput(name = "endMinutes", label = "@text/action.set-ch-schedule-period.input.endMinutes.label", description = "@text/action.set-ch-schedule-period.input.endMinutes.description") int endMinutes,
            @ActionInput(name = "temperatureCelsius", label = "@text/action.set-ch-schedule-period.input.temperatureCelsius.label", description = "@text/action.set-ch-schedule-period.input.temperatureCelsius.description") double temperatureCelsius) {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("setChSchedulePeriod called with no handler bound");
            return false;
        }
        ScheduleDTO schedule = theHandler.composeChSchedulePeriodSet(weekday, periodIndex, startMinutes, endMinutes,
                temperatureCelsius);
        if (schedule == null) {
            logger.warn("setChSchedulePeriod: invalid weekday or periodIndex, or no CH schedule polled yet");
            return false;
        }
        theHandler.sendComposedChSchedule("action:setChSchedulePeriod", schedule);
        return true;
    }

    @RuleAction(label = "@text/action.clear-ch-schedule-period.label", description = "@text/action.clear-ch-schedule-period.description")
    public boolean clearChSchedulePeriod(
            @ActionInput(name = "weekday", label = "@text/action.clear-ch-schedule-period.input.weekday.label", description = "@text/action.clear-ch-schedule-period.input.weekday.description") String weekday,
            @ActionInput(name = "periodIndex", label = "@text/action.clear-ch-schedule-period.input.periodIndex.label", description = "@text/action.clear-ch-schedule-period.input.periodIndex.description") int periodIndex) {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("clearChSchedulePeriod called with no handler bound");
            return false;
        }
        ScheduleDTO schedule = theHandler.composeChSchedulePeriodClear(weekday, periodIndex);
        if (schedule == null) {
            logger.warn("clearChSchedulePeriod: invalid weekday or periodIndex, or no CH schedule polled yet");
            return false;
        }
        theHandler.sendComposedChSchedule("action:clearChSchedulePeriod", schedule);
        return true;
    }

    @RuleAction(label = "@text/action.set-dhw-schedule-period.label", description = "@text/action.set-dhw-schedule-period.description")
    public boolean setDhwSchedulePeriod(
            @ActionInput(name = "weekday", label = "@text/action.set-dhw-schedule-period.input.weekday.label", description = "@text/action.set-dhw-schedule-period.input.weekday.description") String weekday,
            @ActionInput(name = "periodIndex", label = "@text/action.set-dhw-schedule-period.input.periodIndex.label", description = "@text/action.set-dhw-schedule-period.input.periodIndex.description") int periodIndex,
            @ActionInput(name = "startMinutes", label = "@text/action.set-dhw-schedule-period.input.startMinutes.label", description = "@text/action.set-dhw-schedule-period.input.startMinutes.description") int startMinutes,
            @ActionInput(name = "endMinutes", label = "@text/action.set-dhw-schedule-period.input.endMinutes.label", description = "@text/action.set-dhw-schedule-period.input.endMinutes.description") int endMinutes,
            @ActionInput(name = "temperatureCelsius", label = "@text/action.set-dhw-schedule-period.input.temperatureCelsius.label", description = "@text/action.set-dhw-schedule-period.input.temperatureCelsius.description") double temperatureCelsius) {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("setDhwSchedulePeriod called with no handler bound");
            return false;
        }
        ScheduleDTO schedule = theHandler.composeDhwSchedulePeriodSet(weekday, periodIndex, startMinutes, endMinutes,
                temperatureCelsius);
        if (schedule == null) {
            logger.warn("setDhwSchedulePeriod: invalid weekday or periodIndex, or no DHW schedule polled yet");
            return false;
        }
        theHandler.sendComposedDhwSchedule("action:setDhwSchedulePeriod", schedule);
        return true;
    }

    @RuleAction(label = "@text/action.clear-dhw-schedule-period.label", description = "@text/action.clear-dhw-schedule-period.description")
    public boolean clearDhwSchedulePeriod(
            @ActionInput(name = "weekday", label = "@text/action.clear-dhw-schedule-period.input.weekday.label", description = "@text/action.clear-dhw-schedule-period.input.weekday.description") String weekday,
            @ActionInput(name = "periodIndex", label = "@text/action.clear-dhw-schedule-period.input.periodIndex.label", description = "@text/action.clear-dhw-schedule-period.input.periodIndex.description") int periodIndex) {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("clearDhwSchedulePeriod called with no handler bound");
            return false;
        }
        ScheduleDTO schedule = theHandler.composeDhwSchedulePeriodClear(weekday, periodIndex);
        if (schedule == null) {
            logger.warn("clearDhwSchedulePeriod: invalid weekday or periodIndex, or no DHW schedule polled yet");
            return false;
        }
        theHandler.sendComposedDhwSchedule("action:clearDhwSchedulePeriod", schedule);
        return true;
    }

    @RuleAction(label = "@text/action.set-ch-schedule.label", description = "@text/action.set-ch-schedule.description")
    public boolean setChSchedule(
            @ActionInput(name = "json", label = "@text/action.set-ch-schedule.input.json.label", description = "@text/action.set-ch-schedule.input.json.description") String json) {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("setChSchedule called with no handler bound");
            return false;
        }
        ScheduleDTO schedule = theHandler.composeChScheduleFromJson(json);
        if (schedule == null) {
            logger.warn("setChSchedule: malformed JSON, unknown weekday, or no CH schedule polled yet");
            return false;
        }
        theHandler.sendComposedChSchedule("action:setChSchedule", schedule);
        return true;
    }

    @RuleAction(label = "@text/action.set-dhw-schedule.label", description = "@text/action.set-dhw-schedule.description")
    public boolean setDhwSchedule(
            @ActionInput(name = "json", label = "@text/action.set-dhw-schedule.input.json.label", description = "@text/action.set-dhw-schedule.input.json.description") String json) {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("setDhwSchedule called with no handler bound");
            return false;
        }
        ScheduleDTO schedule = theHandler.composeDhwScheduleFromJson(json);
        if (schedule == null) {
            logger.warn("setDhwSchedule: malformed JSON, unknown weekday, or no DHW schedule polled yet");
            return false;
        }
        theHandler.sendComposedDhwSchedule("action:setDhwSchedule", schedule);
        return true;
    }

    @RuleAction(label = "@text/action.cancel-mode.label", description = "@text/action.cancel-mode.description")
    public @ActionOutput(type = "java.lang.Boolean", label = "@text/action.cancel-mode.output.label", description = "@text/action.cancel-mode.output.description") boolean cancelMode() {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("cancelMode called with no handler bound");
            return false;
        }
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();
        boolean requiresPhysicalConfirmation = theHandler.composeCancel(control, configUpdate);
        theHandler.sendComposedUpdate("action:cancelMode", control, configUpdate);
        return requiresPhysicalConfirmation;
    }

    public static void activateVacation(ThingActions actions, long durationSeconds) {
        ((AtagOneActions) actions).activateVacation(durationSeconds);
    }

    public static void activateExtend(ThingActions actions, long durationSeconds) {
        ((AtagOneActions) actions).activateExtend(durationSeconds);
    }

    public static void activateFireplace(ThingActions actions, long durationSeconds) {
        ((AtagOneActions) actions).activateFireplace(durationSeconds);
    }

    public static boolean cancelMode(ThingActions actions) {
        return ((AtagOneActions) actions).cancelMode();
    }

    public static boolean setChSchedulePeriod(ThingActions actions, String weekday, int periodIndex, int startMinutes,
            int endMinutes, double temperatureCelsius) {
        return ((AtagOneActions) actions).setChSchedulePeriod(weekday, periodIndex, startMinutes, endMinutes,
                temperatureCelsius);
    }

    public static boolean clearChSchedulePeriod(ThingActions actions, String weekday, int periodIndex) {
        return ((AtagOneActions) actions).clearChSchedulePeriod(weekday, periodIndex);
    }

    public static boolean setDhwSchedulePeriod(ThingActions actions, String weekday, int periodIndex, int startMinutes,
            int endMinutes, double temperatureCelsius) {
        return ((AtagOneActions) actions).setDhwSchedulePeriod(weekday, periodIndex, startMinutes, endMinutes,
                temperatureCelsius);
    }

    public static boolean clearDhwSchedulePeriod(ThingActions actions, String weekday, int periodIndex) {
        return ((AtagOneActions) actions).clearDhwSchedulePeriod(weekday, periodIndex);
    }

    public static boolean setChSchedule(ThingActions actions, String json) {
        return ((AtagOneActions) actions).setChSchedule(json);
    }

    public static boolean setDhwSchedule(ThingActions actions, String json) {
        return ((AtagOneActions) actions).setDhwSchedule(json);
    }
}
