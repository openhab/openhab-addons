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
package org.openhab.binding.automower.internal.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.automower.internal.things.AutomowerCommand;
import org.openhab.binding.automower.internal.things.AutomowerHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Markus Pfleger - Initial contribution
 */
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
            @ActionInput(name = "duration", label = "@text/action-input-duration-label", description = "@text/action-input-duration-desc") int durationMin) {
        AutomowerHandler automowerHandler = handler;
        if (automowerHandler == null) {
            logger.warn("Automower Action service ThingHandler is null!");
        } else {
            automowerHandler.sendAutomowerCommand(AutomowerCommand.START, durationMin);
        }
    }

    public static void start(ThingActions actions, int durationMin) {
        ((AutomowerActions) actions).start(durationMin);
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
            @ActionInput(name = "duration", label = "@text/action-input-duration-label", description = "@text/action-input-duration-desc") int durationMin) {
        AutomowerHandler automowerHandler = handler;
        if (automowerHandler == null) {
            logger.warn("Automower Action service ThingHandler is null!");
        } else {
            automowerHandler.sendAutomowerCommand(AutomowerCommand.PARK, durationMin);
        }
    }

    public static void park(ThingActions actions, int durationMin) {
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
}
