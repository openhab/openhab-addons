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
package org.openhab.binding.onkyo.internal.automation.modules;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.onkyo.internal.handler.OnkyoHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some automation actions to be used with a {@link OnkyoThingActionsService}
 * <p>
 * <b>Note:</b>The static method <b>invokeMethodOf</b> handles the case where
 * the test <i>actions instanceof OnkyoThingActionsService</i> fails. This test can fail
 * due to an issue in openHAB core v2.5.0 where the {@link OnkyoThingActionsService} class
 * can be loaded by a different classloader than the <i>actions</i> instance.
 *
 * @author David Masshardt - initial contribution
 *
 */
@ThingActionsScope(name = "onkyo")
@NonNullByDefault
public class OnkyoThingActionsService implements ThingActions, OnkyoThingActions {

    private final Logger logger = LoggerFactory.getLogger(OnkyoThingActionsService.class);

    private @Nullable OnkyoHandler handler;

    @Override
    @SuppressWarnings("null")
    @RuleAction(label = "Onkyo sendRawCommand", description = "Action that sends raw command to the receiver")
    public void sendRawCommand(@ActionInput(name = "command") @Nullable String command,
            @ActionInput(name = "command") @Nullable String value) {
        logger.debug("sendRawCommand called with raw command: {} value: {}", command, value);
        if (handler == null) {
            logger.warn("Onkyo Action service ThingHandler is null!");
            return;
        }
        handler.sendRawCommand(command, value);
    }

    public static void sendRawCommand(@Nullable ThingActions actions, @Nullable String command,
            @Nullable String value) {
        invokeMethodOf(actions).sendRawCommand(command, value);
    }

    private static OnkyoThingActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(OnkyoThingActionsService.class.getName())) {
            if (actions instanceof OnkyoThingActions) {
                return (OnkyoThingActions) actions;
            } else {
                return (OnkyoThingActions) Proxy.newProxyInstance(OnkyoThingActions.class.getClassLoader(),
                        new Class[] { OnkyoThingActions.class }, (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException("Actions is not an instance of OnkyoThingActionsService");
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof OnkyoHandler) {
            this.handler = (OnkyoHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }
}
