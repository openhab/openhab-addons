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
package org.openhab.binding.heos.internal.action;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.heos.internal.api.HeosFacade;
import org.openhab.binding.heos.internal.exception.HeosNotConnectedException;
import org.openhab.binding.heos.internal.handler.HeosBridgeHandler;
import org.openhab.binding.heos.internal.resources.Telnet;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class is responsible to call corresponding action on HEOS Handler
 * <p>
 * <b>Note:</b>The static method <b>invokeMethodOf</b> handles the case where
 * the test <i>actions instanceof HeosActions</i> fails. This test can fail
 * due to an issue in openHAB core v2.5.0 where the {@link HeosActions} class
 * can be loaded by a different classloader than the <i>actions</i> instance.
 *
 * @author Martin van Wingerden - Initial contribution
 */
@ThingActionsScope(name = "heos")
@NonNullByDefault
public class HeosActions implements ThingActions, IHeosActions {

    private final static Logger logger = LoggerFactory.getLogger(HeosActions.class);

    private @Nullable HeosBridgeHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof HeosBridgeHandler) {
            this.handler = (HeosBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    private @Nullable HeosFacade getConnection() throws HeosNotConnectedException {
        if (handler == null) {
            return null;
        }

        return handler.getApiConnection();
    }

    @Override
    @RuleAction(label = "Play Input", description = "Play an input from another device")
    public void playInputFromPlayer(
            @ActionInput(name = "source", label = "Source Player", description = "Player used for input") @Nullable Integer sourcePlayer,
            @ActionInput(name = "input", label = "Source Input", description = "Input source used") @Nullable String input,
            @ActionInput(name = "destination", label = "Destination Player", description = "Device for audio output") @Nullable Integer destinationPlayer) {
        if (sourcePlayer == null || input == null || destinationPlayer == null) {
            logger.debug(
                    "Skipping HEOS playInputFromPlayer due to null value: sourcePlayer: {}, input: {}, destination: {}",
                    sourcePlayer, input, destinationPlayer);
            return;
        }

        try {
            HeosFacade connection = getConnection();

            if (connection == null) {
                logger.debug("Skipping HEOS playInputFromPlayer because no connection was available");
                return;
            }

            connection.playInputSource(destinationPlayer.toString(), sourcePlayer.toString(), input);
        } catch (IOException | Telnet.ReadException e) {
            logger.warn("Failed to play input source!", e);
        }
    }

    public static void playInputFromPlayer(@Nullable ThingActions actions, @Nullable Integer sourcePlayer,
            @Nullable String input, @Nullable Integer destinationPlayer) {
        invokeMethodOf(actions).playInputFromPlayer(sourcePlayer, input, destinationPlayer);
    }

    private static IHeosActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(HeosActions.class.getName())) {
            if (actions instanceof IHeosActions) {
                return (IHeosActions) actions;
            } else {
                return (IHeosActions) Proxy.newProxyInstance(IHeosActions.class.getClassLoader(),
                        new Class[] { IHeosActions.class }, (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException("Actions is not an instance of HeosActions");
    }
}
