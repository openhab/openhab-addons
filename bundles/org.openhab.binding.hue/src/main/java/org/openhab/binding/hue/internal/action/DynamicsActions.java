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
import org.openhab.binding.hue.internal.handler.Clip2ThingHandler;
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
 * Implementation of the {@link ThingActions} interface used for sending 'dynamics' commands to Hue API v2 devices,
 * rooms or zones.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@ThingActionsScope(name = "hue")
@NonNullByDefault
public class DynamicsActions implements ThingActions {

    public static void dynamicCommand(ThingActions actions, @Nullable String channelId, @Nullable Command command,
            @Nullable DecimalType durationMSec) {
        ((DynamicsActions) actions).dynamicCommand(channelId, command, durationMSec);
    }

    private final Logger logger = LoggerFactory.getLogger(DynamicsActions.class);

    private @Nullable Clip2ThingHandler handler;

    @RuleAction(label = "@text/dynamics.action.label", description = "@text/dynamics.action.description")
    public void dynamicCommand(
            @ActionInput(name = "channelId", label = "@text/dynamics.channel-id.label", description = "@text/dynamics.channel-id.description") @Nullable String channelId,
            @ActionInput(name = "command", label = "@text/dynamics.command.label", description = "@text/dynamics.command.description") @Nullable Command command,
            @ActionInput(name = "durationMSec", label = "@text/dynamics.duration.label", description = "@text/dynamics.duration.description") @Nullable DecimalType durationMSec) {
        //
        Clip2ThingHandler handler = this.handler;
        if (handler == null) {
            logger.warn("ThingHandler is null!");
            return;
        }
        if (channelId == null) {
            logger.debug("Channel ID is null.");
            return;
        }
        if (command == null) {
            logger.debug("Command is null.");
            return;
        }
        if (durationMSec == null || durationMSec.longValue() <= 0) {
            logger.debug("Duration is null, zero or negative.");
            return;
        }
        handler.handleDynamicsCommand(channelId, command, durationMSec);
        logger.debug("Dynamic command '{}' sent to channelId '{}' with duration {}mSec.", command, channelId,
                durationMSec);
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (Clip2ThingHandler) handler;
    }
}
