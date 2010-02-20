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
package org.openhab.binding.mpd.internal.action;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.mpd.internal.handler.MPDHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link @MPDActions} defines rule actions for the Music Player Daemon binding.
 *
 * @author Stefan Röllin - Initial contribution
 */
@ThingActionsScope(name = "mpd")
@NonNullByDefault
public class MPDActions implements ThingActions, IMPDActions {

    private final Logger logger = LoggerFactory.getLogger(MPDActions.class);

    private @Nullable MPDHandler handler = null;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof MPDHandler) {
            this.handler = (MPDHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @Override
    @RuleAction(label = "MPD : Send command", description = "Send a command to the Music Player Daemon.")
    public void sendCommand(@ActionInput(name = "command") @Nullable String command,
            @ActionInput(name = "parameter") @Nullable String parameter) {
        logger.debug("sendCommand called with {}", command);

        MPDHandler handler = this.handler;
        if (handler != null) {
            handler.sendCommand(command, parameter);
        } else {
            logger.warn("MPD Action service ThingHandler is null!");
        }
    }

    @Override
    @RuleAction(label = "MPD : Send command", description = "Send a command to the Music Player Daemon.")
    public void sendCommand(@ActionInput(name = "command") @Nullable String command) {
        logger.debug("sendCommand called with {}", command);

        MPDHandler handler = this.handler;
        if (handler != null) {
            handler.sendCommand(command);
        } else {
            logger.warn("MPD Action service ThingHandler is null!");
        }
    }

    private static IMPDActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(MPDActions.class.getName())) {
            if (actions instanceof IMPDActions) {
                return (IMPDActions) actions;
            } else {
                return (IMPDActions) Proxy.newProxyInstance(IMPDActions.class.getClassLoader(),
                        new Class[] { IMPDActions.class }, (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException("Actions is not an instance of MPDActions");
    }

    public static void sendCommand(@Nullable ThingActions actions, @Nullable String command,
            @Nullable String parameter) {
        invokeMethodOf(actions).sendCommand(command, parameter);
    }

    public static void sendCommand(@Nullable ThingActions actions, @Nullable String command) {
        invokeMethodOf(actions).sendCommand(command);
    }
}
