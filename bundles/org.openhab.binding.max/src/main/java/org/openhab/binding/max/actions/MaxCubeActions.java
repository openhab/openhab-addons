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
package org.openhab.binding.max.actions;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.max.internal.actions.IMaxCubeActions;
import org.openhab.binding.max.internal.handler.MaxCubeBridgeHandler;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MaxCubeActions} class defines rule actions for MAX! Cube
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@ThingActionsScope(name = "max-cube")
@NonNullByDefault
public class MaxCubeActions implements ThingActions, IMaxCubeActions {

    private final Logger logger = LoggerFactory.getLogger(MaxCubeActions.class);

    private @Nullable MaxCubeBridgeHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof MaxCubeBridgeHandler) {
            this.handler = (MaxCubeBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    @Override
    @RuleAction(label = "Backup Cube Data", description = "Creates a backup of the MAX! Cube data.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean backup() {
        MaxCubeBridgeHandler actionsHandler = handler;
        if (actionsHandler == null) {
            logger.info("MaxCubeActions: Action service ThingHandler is null!");
            return false;
        }
        actionsHandler.backup();
        return true;
    }

    public static boolean backup(@Nullable ThingActions actions) {
        return invokeMethodOf(actions).backup();
    }

    @Override
    @RuleAction(label = "Reset Cube Configuration", description = "Resets the MAX! Cube room and device information. Devices will need to be included again!")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean resetConfig() {
        MaxCubeBridgeHandler actionsHandler = handler;
        if (actionsHandler == null) {
            logger.info("MaxCubeActions: Action service ThingHandler is null!");
            return false;
        }
        actionsHandler.cubeConfigReset();
        return true;
    }

    public static boolean reset(@Nullable ThingActions actions) {
        return invokeMethodOf(actions).resetConfig();
    }

    @Override
    @RuleAction(label = "Restart Cube", description = "Restarts the MAX! Cube.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean reboot() {
        MaxCubeBridgeHandler actionsHandler = handler;
        if (actionsHandler == null) {
            logger.info("MaxCubeActions: Action service ThingHandler is null!");
            return false;
        }
        actionsHandler.cubeReboot();
        return true;
    }

    public static boolean reboot(@Nullable ThingActions actions) {
        return invokeMethodOf(actions).reboot();
    }

    private static IMaxCubeActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(MaxCubeActions.class.getName())) {
            if (actions instanceof IMaxCubeActions) {
                return (IMaxCubeActions) actions;
            } else {
                return (IMaxCubeActions) Proxy.newProxyInstance(IMaxCubeActions.class.getClassLoader(),
                        new Class[] { IMaxCubeActions.class }, (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException("Actions is not an instance of MaxCubeActions");
    }
}
