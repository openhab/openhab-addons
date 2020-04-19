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
 * The {@link @MPDActions} defines rule actions for the Music Player Daemon binding. *
 *
 * @author Stefan Röllin - Initial contribution
 */
@ThingActionsScope(name = "mpd")
@NonNullByDefault
public class MPDActions implements ThingActions {

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

    @RuleAction(label = "MPD : Send command", description = "Send a command to the Music Player Daemon.")
    public void sendCommand(@ActionInput(name = "command") @Nullable String command,
            @ActionInput(name = "parameter") @Nullable String parameter) {
        logger.debug("sendCommand called with {}", command);

        if (handler != null) {
            handler.sendCommand(command, parameter);
        } else {
            logger.warn("MPD Action service ThingHandler is null!");
        }
    }

    @RuleAction(label = "MPD : Send command", description = "Send a command to the Music Player Daemon.")
    public void sendCommand(@ActionInput(name = "command") @Nullable String command) {
        logger.debug("sendCommand called with {}", command);

        if (handler != null) {
            handler.sendCommand(command);
        } else {
            logger.warn("MPD Action service ThingHandler is null!");
        }
    }

    public static void sendCommand(@Nullable ThingActions actions, @Nullable String command,
            @Nullable String parameter) {
        if (actions instanceof MPDActions) {
            ((MPDActions) actions).sendCommand(command, parameter);
        } else {
            throw new IllegalArgumentException("Instance is not an MPDActions class.");
        }
    }

    public static void sendCommand(@Nullable ThingActions actions, @Nullable String command) {
        if (actions instanceof MPDActions) {
            ((MPDActions) actions).sendCommand(command);
        } else {
            throw new IllegalArgumentException("Instance is not an MPDActions class.");
        }
    }
}
