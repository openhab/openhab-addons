/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.handler.HueLightActionsHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LightActions} defines {@link ThingActions} for the Hue lights.
 *
 * @author Jochen Leopold - Initial contribution
 */
@ThingActionsScope(name = "hue")
@NonNullByDefault
public class LightActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(LightActions.class);
    private @Nullable HueLightActionsHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (HueLightActionsHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/actionLabel", description = "@text/actionDesc")
    public void fadingLightCommand(
            @ActionInput(name = "channel", label = "@text/actionInputChannelLabel", description = "@text/actionInputChannelDesc") @Nullable String channel,
            @ActionInput(name = "command", label = "@text/actionInputCommandLabel", description = "@text/actionInputCommandDesc") @Nullable Command command,
            @ActionInput(name = "fadeTime", label = "@text/actionInputFadeTimeLabel", description = "@text/actionInputFadeTimeDesc") @Nullable DecimalType fadeTime) {
        HueLightActionsHandler lightActionsHandler = handler;
        if (lightActionsHandler == null) {
            logger.warn("Hue Action service ThingHandler is null!");
            return;
        }

        if (channel == null) {
            logger.debug("skipping Hue fadingLightCommand to channel '{}' due to null value.", channel);
            return;
        }
        if (command == null) {
            logger.debug("skipping Hue fadingLightCommand to command '{}' due to null value.", command);
            return;
        }
        if (fadeTime == null) {
            logger.debug("skipping Hue fadingLightCommand to fadeTime '{}' due to null value.", fadeTime);
            return;
        }

        lightActionsHandler.handleCommand(channel, command, fadeTime.longValue());
        logger.debug("send fadingLightCommand to channel '{}' with fadeTime of {}ms.", channel, fadeTime);
    }

    public static void fadingLightCommand(ThingActions actions, @Nullable String channel, @Nullable Command command,
            @Nullable DecimalType fadeTime) {
        ((LightActions) actions).fadingLightCommand(channel, command, fadeTime);
    }
}
