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
package org.openhab.binding.zm.action;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.zm.internal.handler.ZmMonitorHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZmActions} defines the thing actions provided by this binding.
 *
 * <b>Note:</b>The static method <b>invokeMethodOf</b> handles the case where
 * the test <i>actions instanceof ZmActions</i> fails. This test can fail
 * due to an issue in openHAB core v2.5.0 where the {@link ZmActions} class
 * can be loaded by a different classloader than the <i>actions</i> instance.
 *
 * @author Mark Hilbush - Initial contribution
 */
@ThingActionsScope(name = "zm")
@NonNullByDefault
public class ZmActions implements ThingActions, IZmActions {
    private final Logger logger = LoggerFactory.getLogger(ZmActions.class);

    private @Nullable ZmMonitorHandler handler;

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof ZmMonitorHandler) {
            this.handler = (ZmMonitorHandler) handler;
        }
    }

    private static IZmActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(ZmActions.class.getName())) {
            if (actions instanceof IZmActions) {
                return (IZmActions) actions;
            } else {
                return (IZmActions) Proxy.newProxyInstance(IZmActions.class.getClassLoader(),
                        new Class[] { IZmActions.class }, (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException("Actions is not an instance of ZmActions");
    }

    /**
     * The Trigger Alarm function triggers an alarm that will run for the number of seconds
     * specified by the supplied parameter duration.
     */
    @Override
    @RuleAction(label = "TriggerAlarm", description = "Trigger an alarm on the monitor.")
    public void triggerAlarm(
            @ActionInput(name = "duration", description = "The duration of the alarm in seconds.") @Nullable Number duration) {
        logger.debug("ZmActions: Action 'TriggerAlarm' called");
        ZmMonitorHandler localHandler = handler;
        if (localHandler == null) {
            logger.info("ZmActions: Action service ThingHandler is null!");
            return;
        }
        localHandler.actionTriggerAlarm(duration);
    }

    public static void triggerAlarm(@Nullable ThingActions actions, @Nullable Number alarmDuration) {
        invokeMethodOf(actions).triggerAlarm(alarmDuration);
    }

    /**
     * The Trigger Alarm function triggers an alarm that will run for the number of seconds
     * specified in the thing configuration.
     */
    @Override
    @RuleAction(label = "TriggerAlarm", description = "Trigger an alarm on the monitor.")
    public void triggerAlarm() {
        logger.debug("ZmActions: Action 'TriggerAlarm' called");
        ZmMonitorHandler localHandler = handler;
        if (localHandler == null) {
            logger.info("ZmActions: Action service ThingHandler is null!");
            return;
        }
        localHandler.actionTriggerAlarm();
    }

    public static void triggerAlarm(@Nullable ThingActions actions) {
        invokeMethodOf(actions).triggerAlarm();
    }

    /**
     * The Cancel Alarm function cancels a running alarm.
     */
    @Override
    @RuleAction(label = "CancelAlarm", description = "Cancel a running alarm.")
    public void cancelAlarm() {
        logger.debug("ZmActions: Action 'CancelAlarm' called");
        ZmMonitorHandler localHandler = handler;
        if (localHandler == null) {
            logger.info("ZmActions: Action service ThingHandler is null!");
            return;
        }
        localHandler.actionCancelAlarm();
    }

    public static void cancelAlarm(@Nullable ThingActions actions) {
        invokeMethodOf(actions).cancelAlarm();
    }
}
