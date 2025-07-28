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
package org.openhab.binding.automower.internal.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.HeadlightMode;
import org.openhab.binding.automower.internal.things.AutomowerCommand;
import org.openhab.binding.automower.internal.things.AutomowerHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Markus Pfleger - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = AutomowerActions.class)
@ThingActionsScope(name = "automower")
@NonNullByDefault
public class AutomowerActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(AutomowerActions.class);
    private @Nullable AutomowerHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (AutomowerHandler) handler;
    }

    @Override
    public @Nullable AutomowerHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/action-start-label", description = "@text/action-start-desc")
    public void start(
            @ActionInput(name = "duration-min", label = "@text/action-input-duration-min-label", description = "@text/action-input-duration-min-desc") long durationMin) {
        AutomowerHandler automowerHandler = handler;
        if (automowerHandler == null) {
            logger.warn("Automower Action service ThingHandler is null!");
        } else {
            automowerHandler.sendAutomowerCommand(AutomowerCommand.START, durationMin);
        }
    }

    public static void start(ThingActions actions, long durationMin) {
        ((AutomowerActions) actions).start(durationMin);
    }

    @RuleAction(label = "@text/action-start-in-work-area-label", description = "@text/action-start-in-work-area-desc")
    public void startInWorkArea(
            @ActionInput(name = "work-area-id", label = "@text/action-input-work-area-id-label", description = "@text/action-input-work-area-id-desc") long workAreaId,
            @ActionInput(name = "duration-min-opt", label = "@text/action-input-duration-min-opt-label", description = "@text/action-input-duration-min-opt-desc") @Nullable Long durationMin) {
        AutomowerHandler automowerHandler = handler;
        if (automowerHandler == null) {
            logger.warn("Automower Action service ThingHandler is null!");
        } else {
            automowerHandler.sendAutomowerCommand(AutomowerCommand.START_IN_WORK_AREA, workAreaId, durationMin);
        }
    }

    public static void startInWorkArea(ThingActions actions, long workAreaId, @Nullable Long durationMin) {
        ((AutomowerActions) actions).startInWorkArea(workAreaId, durationMin);
    }

    @RuleAction(label = "@text/action-pause-label", description = "@text/action-pause-desc")
    public void pause() {
        AutomowerHandler automowerHandler = handler;
        if (automowerHandler == null) {
            logger.warn("Automower Action service ThingHandler is null!");
        } else {
            automowerHandler.sendAutomowerCommand(AutomowerCommand.PAUSE);
        }
    }

    public static void pause(ThingActions actions) {
        ((AutomowerActions) actions).pause();
    }

    @RuleAction(label = "@text/action-parkuntilnextschedule-label", description = "@text/action-parkuntilnextschedule-desc")
    public void parkUntilNextSchedule() {
        AutomowerHandler automowerHandler = handler;
        if (automowerHandler == null) {
            logger.warn("Automower Action service ThingHandler is null!");
        } else {
            automowerHandler.sendAutomowerCommand(AutomowerCommand.PARK_UNTIL_NEXT_SCHEDULE);
        }
    }

    public static void parkUntilNextSchedule(ThingActions actions) {
        ((AutomowerActions) actions).parkUntilNextSchedule();
    }

    @RuleAction(label = "@text/action-parkuntilfurthernotice-label", description = "@text/action-parkuntilfurthernotice-desc")
    public void parkUntilFurtherNotice() {
        AutomowerHandler automowerHandler = handler;
        if (automowerHandler == null) {
            logger.warn("Automower Action service ThingHandler is null!");
        } else {
            automowerHandler.sendAutomowerCommand(AutomowerCommand.PARK_UNTIL_FURTHER_NOTICE);
        }
    }

    public static void parkUntilFurtherNotice(ThingActions actions) {
        ((AutomowerActions) actions).parkUntilFurtherNotice();
    }

    @RuleAction(label = "@text/action-park-label", description = "@text/action-park-desc")
    public void park(
            @ActionInput(name = "duration-min", label = "@text/action-input-duration-min-label", description = "@text/action-input-duration-min-desc") long durationMin) {
        AutomowerHandler automowerHandler = handler;
        if (automowerHandler == null) {
            logger.warn("Automower Action service ThingHandler is null!");
        } else {
            automowerHandler.sendAutomowerCommand(AutomowerCommand.PARK, durationMin);
        }
    }

    public static void park(ThingActions actions, long durationMin) {
        ((AutomowerActions) actions).park(durationMin);
    }

    @RuleAction(label = "@text/action-resumeschedule-label", description = "@text/action-resumeschedule-desc")
    public void resumeSchedule() {
        AutomowerHandler automowerHandler = handler;
        if (automowerHandler == null) {
            logger.warn("Automower Action service ThingHandler is null!");
        } else {
            automowerHandler.sendAutomowerCommand(AutomowerCommand.RESUME_SCHEDULE);
        }
    }

    public static void resumeSchedule(ThingActions actions) {
        ((AutomowerActions) actions).resumeSchedule();
    }

    @RuleAction(label = "@text/action-confirm-error-label", description = "@text/action-confirm-error-desc")
    public void confirmError() {
        AutomowerHandler automowerHandler = handler;
        if (automowerHandler == null) {
            logger.warn("Automower Action service ThingHandler is null!");
        } else {
            automowerHandler.sendAutomowerConfirmError();
        }
    }

    public static void confirmError(ThingActions actions) {
        ((AutomowerActions) actions).confirmError();
    }

    @RuleAction(label = "@text/action-reset-cutting-blade-usage-time-label", description = "@text/action-reset-cutting-blade-usage-time-desc")
    public void resetCuttingBladeUsageTime() {
        AutomowerHandler automowerHandler = handler;
        if (automowerHandler == null) {
            logger.warn("Automower Action service ThingHandler is null!");
        } else {
            automowerHandler.sendAutomowerResetCuttingBladeUsageTime();
        }
    }

    public static void resetCuttingBladeUsageTime(ThingActions actions) {
        ((AutomowerActions) actions).resetCuttingBladeUsageTime();
    }

    @RuleAction(label = "@text/action-set-settings-label", description = "@text/action-set-settings-desc")
    public void setSettings(
            @ActionInput(name = "cutting-height", label = "@text/action-input-cutting-height-label", description = "@text/action-input-cutting-height-desc") @Nullable Byte cuttingHeight,
            @ActionInput(name = "headlight-mode", label = "@text/action-input-headlight-mode-label", description = "@text/action-input-headlight-mode-desc") @Nullable String headlightMode) {
        AutomowerHandler automowerHandler = handler;
        if (automowerHandler == null) {
            logger.warn("Automower Action service ThingHandler is null!");
        } else {
            try {
                if (headlightMode != null) {
                    automowerHandler.sendAutomowerSettings(cuttingHeight, HeadlightMode.valueOf(headlightMode));
                } else {
                    automowerHandler.sendAutomowerSettings(cuttingHeight, null);
                }
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid HeadlightMode: {}, Error: {}", headlightMode, e.getMessage());
            }
        }
    }

    public static void setSettings(ThingActions actions, @Nullable Byte cuttingHeight, @Nullable String headlightMode) {
        ((AutomowerActions) actions).setSettings(cuttingHeight, headlightMode);
    }

    @RuleAction(label = "@text/action-set-work-area-label", description = "@text/action-set-work-area-desc")
    public void setWorkArea(
            @ActionInput(name = "work-area-id", label = "@text/action-input-work-area-id-label", description = "@text/action-input-work-area-id-desc") long workAreaId,
            @ActionInput(name = "enable", label = "@text/action-input-enable-label", description = "@text/action-input-enable-desc") boolean enable,
            @ActionInput(name = "cutting-height", label = "@text/action-input-cutting-height-label", description = "@text/action-input-cutting-height-desc") byte cuttingHeight) {
        AutomowerHandler automowerHandler = handler;
        if (automowerHandler == null) {
            logger.warn("Automower Action service ThingHandler is null!");
        } else {
            automowerHandler.sendAutomowerWorkArea(workAreaId, enable, cuttingHeight);
        }
    }

    public static void setWorkArea(ThingActions actions, long workAreaId, boolean enable, byte cuttingHeight) {
        ((AutomowerActions) actions).setWorkArea(workAreaId, enable, cuttingHeight);
    }

    @RuleAction(label = "@text/action-set-stayoutzone-label", description = "@text/action-set-stayoutzone-desc")
    public void setStayOutZone(
            @ActionInput(name = "zone-id", label = "@text/action-input-zone-id-label", description = "@text/action-input-zone-id-desc") String zoneId,
            @ActionInput(name = "enable", label = "@text/action-input-enable-label", description = "@text/action-input-enable-desc") boolean enable) {
        AutomowerHandler automowerHandler = handler;
        if (automowerHandler == null) {
            logger.warn("Automower Action service ThingHandler is null!");
        } else {
            automowerHandler.sendAutomowerStayOutZone(zoneId, enable);
        }
    }

    public static void setStayOutZone(ThingActions actions, String zoneId, boolean enable) {
        ((AutomowerActions) actions).setStayOutZone(zoneId, enable);
    }

    @RuleAction(label = "@text/action-set-calendartask-label", description = "@text/action-set-calendartask-desc")
    public void setCalendarTask(
            @ActionInput(name = "work-area-id", label = "@text/action-input-work-area-id-label", description = "@text/action-input-work-area-id-desc") @Nullable Long workAreaId,
            @ActionInput(name = "start", label = "@text/action-input-start-label", description = "@text/action-input-start-desc") short[] start,
            @ActionInput(name = "duration", label = "@text/action-input-duration-label", description = "@text/action-input-duration-desc") short[] duration,
            @ActionInput(name = "monday", label = "@text/action-input-monday-label", description = "@text/action-input-monday-desc") boolean[] monday,
            @ActionInput(name = "tuesday", label = "@text/action-input-tuesday-label", description = "@text/action-input-tuesday-desc") boolean[] tuesday,
            @ActionInput(name = "wednesday", label = "@text/action-input-wednesday-label", description = "@text/action-input-wednesday-desc") boolean[] wednesday,
            @ActionInput(name = "thursday", label = "@text/action-input-thursday-label", description = "@text/action-input-thursday-desc") boolean[] thursday,
            @ActionInput(name = "friday", label = "@text/action-input-friday-label", description = "@text/action-input-friday-desc") boolean[] friday,
            @ActionInput(name = "saturday", label = "@text/action-input-saturday-label", description = "@text/action-input-saturday-desc") boolean[] saturday,
            @ActionInput(name = "sunday", label = "@text/action-input-sunday-label", description = "@text/action-input-sunday-desc") boolean[] sunday) {
        AutomowerHandler automowerHandler = handler;
        if (automowerHandler == null) {
            logger.warn("Automower Action service ThingHandler is null!");
        } else {
            automowerHandler.sendAutomowerCalendarTask(workAreaId, start, duration, monday, tuesday, wednesday,
                    thursday, friday, saturday, sunday);
        }
    }

    public static void setCalendarTask(ThingActions actions, @Nullable Long workAreaId, short[] start, short[] duration,
            boolean[] monday, boolean[] tuesday, boolean[] wednesday, boolean[] thursday, boolean[] friday,
            boolean[] saturday, boolean[] sunday) {
        ((AutomowerActions) actions).setCalendarTask(workAreaId, start, duration, monday, tuesday, wednesday, thursday,
                friday, saturday, sunday);
    }

    @RuleAction(label = "@text/action-poll-label", description = "@text/action-poll-desc")
    public void poll() {
        AutomowerHandler automowerHandler = handler;
        if (automowerHandler == null) {
            logger.warn("Automower Action service ThingHandler is null!");
        } else {
            automowerHandler.poll();
        }
    }

    public static void poll(ThingActions actions) {
        ((AutomowerActions) actions).poll();
    }
}
