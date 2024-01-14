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
package org.openhab.binding.zoneminder.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zoneminder.internal.handler.ZmMonitorHandler;
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
 * The {@link ZmActions} defines the thing actions provided by this binding.
 *
 * @author Mark Hilbush - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = ZmActions.class)
@ThingActionsScope(name = "zoneminder")
@NonNullByDefault
public class ZmActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(ZmActions.class);

    private @Nullable ZmMonitorHandler handler;

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof ZmMonitorHandler zmMonitorHandler) {
            this.handler = zmMonitorHandler;
        }
    }

    /**
     * The Trigger Alarm function triggers an alarm that will run for the number of seconds
     * specified by the supplied parameter duration.
     */
    @RuleAction(label = "trigger an alarm", description = "Trigger an alarm on the monitor.")
    public void triggerAlarm(
            @ActionInput(name = "duration", description = "The duration of the alarm in seconds.") @Nullable Number duration) {
        logger.debug("ZmActions: Action 'TriggerAlarm' called");
        ZmMonitorHandler localHandler = handler;
        if (localHandler == null) {
            logger.warn("ZmActions: Action service ThingHandler is null!");
            return;
        }
        localHandler.actionTriggerAlarm(duration);
    }

    public static void triggerAlarm(ThingActions actions, @Nullable Number alarmDuration) {
        ((ZmActions) actions).triggerAlarm(alarmDuration);
    }

    /**
     * The Trigger Alarm function triggers an alarm that will run for the number of seconds
     * specified in the thing configuration.
     */
    @RuleAction(label = "trigger an alarm", description = "Trigger an alarm on the monitor.")
    public void triggerAlarm() {
        logger.debug("ZmActions: Action 'TriggerAlarm' called");
        ZmMonitorHandler localHandler = handler;
        if (localHandler == null) {
            logger.warn("ZmActions: Action service ThingHandler is null!");
            return;
        }
        localHandler.actionTriggerAlarm();
    }

    public static void triggerAlarm(ThingActions actions) {
        ((ZmActions) actions).triggerAlarm();
    }

    /**
     * The Cancel Alarm function cancels a running alarm.
     */
    @RuleAction(label = "cancel a running alarm", description = "Cancel a running alarm.")
    public void cancelAlarm() {
        logger.debug("ZmActions: Action 'CancelAlarm' called");
        ZmMonitorHandler localHandler = handler;
        if (localHandler == null) {
            logger.warn("ZmActions: Action service ThingHandler is null!");
            return;
        }
        localHandler.actionCancelAlarm();
    }

    public static void cancelAlarm(ThingActions actions) {
        ((ZmActions) actions).cancelAlarm();
    }
}
