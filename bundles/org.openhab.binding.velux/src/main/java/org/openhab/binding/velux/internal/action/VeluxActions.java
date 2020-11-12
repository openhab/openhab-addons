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
package org.openhab.binding.velux.internal.action;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.handler.VeluxBridgeHandler;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeluxActions} implementation of the rule action for rebooting the bridge
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@ThingActionsScope(name = "velux")
@NonNullByDefault
public class VeluxActions implements ThingActions, IVeluxActions {

    private final Logger logger = LoggerFactory.getLogger(VeluxActions.class);

    private @Nullable VeluxBridgeHandler bridgeHandler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof VeluxBridgeHandler) {
            this.bridgeHandler = (VeluxBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.bridgeHandler;
    }

    @Override
    @RuleAction(label = "Reboot Bridge", description = "issues a reboot command to the KLF200 bridge")
    public @ActionOutput(name = "executing", type = "java.lang.Boolean") Boolean rebootBridge() {
        logger.trace("rebootBridge(): reboot action called");
        VeluxBridgeHandler bridgeHandler = this.bridgeHandler;
        if (bridgeHandler != null) {
            return bridgeHandler.runReboot();
        }
        return false;
    }

    public static boolean rebootBridge(@Nullable ThingActions actions) {
        return invokeMethodOf(actions).rebootBridge();
    }

    private static IVeluxActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(VeluxActions.class.getName())) {
            if (actions instanceof IVeluxActions) {
                return (IVeluxActions) actions;
            } else {
                return (IVeluxActions) Proxy.newProxyInstance(IVeluxActions.class.getClassLoader(),
                        new Class[] { IVeluxActions.class }, (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException("Actions is not an instance of VeluxActions");
    }
}
