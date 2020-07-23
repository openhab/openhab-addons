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
package org.openhab.binding.freebox.internal.action;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.freebox.internal.handler.ServerHandler;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {ServerActions} class is responsible to call corresponding
 * actions on Freebox Server
 *
 * @author Gaël L'hopital - Initial contribution
 */
@ThingActionsScope(name = "freebox")
@NonNullByDefault
public class ServerActions implements ThingActions, IServerActions {
    private final static Logger logger = LoggerFactory.getLogger(ServerActions.class);
    private @Nullable ServerHandler handler;

    public ServerActions() {
        logger.info("Freebox Server actions service instanciated");
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof ServerHandler) {
            this.handler = (ServerHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    @Override
    @RuleAction(label = "Freebox : Reboot", description = "Reboots the Freebox Server")
    public void reboot() {
        logger.debug("Server reboot called");
        if (handler != null) {
            handler.reboot();
        } else {
            logger.warn("Freebox Action service ThingHandler is null!");
        }
    }

    public static void reboot(@Nullable ThingActions actions) {
        invokeMethodOf(actions).reboot();
    }

    private static IServerActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(ServerActions.class.getName())) {
            if (actions instanceof IServerActions) {
                return (IServerActions) actions;
            } else {
                return (IServerActions) Proxy.newProxyInstance(IServerActions.class.getClassLoader(),
                        new Class[] { IServerActions.class }, (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException("Actions is not an instance of AstroActions");
    }
}
