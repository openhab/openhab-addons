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
package org.openhab.binding.heos.internal.action;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.heos.internal.api.HeosFacade;
import org.openhab.binding.heos.internal.exception.HeosNotConnectedException;
import org.openhab.binding.heos.internal.handler.HeosBridgeHandler;
import org.openhab.binding.heos.internal.resources.Telnet;
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
 * The class is responsible to call corresponding action on HEOS Handler
 *
 * @author Martin van Wingerden - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = HeosActions.class)
@ThingActionsScope(name = "heos")
@NonNullByDefault
public class HeosActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(HeosActions.class);

    private @Nullable HeosBridgeHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof HeosBridgeHandler bridgeHandler) {
            this.handler = bridgeHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    private @Nullable HeosFacade getConnection() throws HeosNotConnectedException {
        HeosBridgeHandler handler = this.handler;
        if (handler == null) {
            return null;
        }

        return handler.getApiConnection();
    }

    @RuleAction(label = "play an input", description = "Play an input from another device.")
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

    public static void playInputFromPlayer(ThingActions actions, @Nullable Integer sourcePlayer, @Nullable String input,
            @Nullable Integer destinationPlayer) {
        ((HeosActions) actions).playInputFromPlayer(sourcePlayer, input, destinationPlayer);
    }
}
