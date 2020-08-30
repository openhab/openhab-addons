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
package org.openhab.binding.avmfritz.actions;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.avmfritz.internal.actions.AVMFritzHeatingActionsHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AVMFritzHeatingActions} defines thing actions for heating devices / groups of the avmfritz binding.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@ThingActionsScope(name = "avmfritz")
@NonNullByDefault
public class AVMFritzHeatingActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(AVMFritzHeatingActions.class);

    private @Nullable AVMFritzHeatingActionsHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (AVMFritzHeatingActionsHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/setBoostModeModeActionLabel", description = "@text/setBoostModeActionDescription")
    public void setBoostMode(
            @ActionInput(name = "Duration", label = "@text/setBoostModeDurationInputLabel", description = "@text/setBoostModeDurationInputDescription", type = "java.lang.Long", required = true) Long duration) {
        AVMFritzHeatingActionsHandler actionsHandler = handler;
        if (actionsHandler == null) {
            logger.warn("AVMFritzHeatingActions ThingHandler is null!");
            return;
        }
        actionsHandler.setBoostMode(duration.longValue());
    }

    public static void setBoostMode(@Nullable ThingActions actions, Long duration) {
        invokeMethodOf(actions).setBoostMode(duration);
    }

    @RuleAction(label = "@text/setWindowOpenModeActionLabel", description = "@text/setWindowOpenModeActionDescription")
    public void setWindowOpenMode(
            @ActionInput(name = "Duration", label = "@text/setWindowOpenModeDurationInputLabel", description = "@text/setWindowOpenModeDurationInputDescription", type = "java.lang.Long", required = true) Long duration) {
        AVMFritzHeatingActionsHandler actionsHandler = handler;
        if (actionsHandler == null) {
            logger.warn("AVMFritzHeatingActions ThingHandler is null!");
            return;
        }
        actionsHandler.setWindowOpenMode(duration.longValue());
    }

    public static void setWindowOpenMode(@Nullable ThingActions actions, Long duration) {
        invokeMethodOf(actions).setWindowOpenMode(duration);
    }

    private static AVMFritzHeatingActionsHandler invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("Actions cannot be null");
        }
        if (actions.getClass().getName().equals(AVMFritzHeatingActionsHandler.class.getName())) {
            if (actions instanceof AVMFritzHeatingActionsHandler) {
                return (AVMFritzHeatingActionsHandler) actions;
            } else {
                return (AVMFritzHeatingActionsHandler) Proxy.newProxyInstance(
                        AVMFritzHeatingActionsHandler.class.getClassLoader(),
                        new Class[] { AVMFritzHeatingActionsHandler.class },
                        (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException("Actions is not an instance of AVMFritzHeatingActionsHandler");
    }
}
