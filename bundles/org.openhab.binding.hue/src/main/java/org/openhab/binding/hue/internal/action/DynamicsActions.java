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
package org.openhab.binding.hue.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.handler.Clip2ThingHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link ThingActions} interface used for sending 'dynamics' commands to Hue API v2 devices,
 * rooms or zones.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = DynamicsActions.class)
@ThingActionsScope(name = "hue")
@NonNullByDefault
public class DynamicsActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(DynamicsActions.class);

    private @Nullable Clip2ThingHandler handler;

    public static void dynamicCommand(ThingActions actions, @Nullable String channelId, @Nullable Command command,
            @Nullable Long durationMs) {
        ((DynamicsActions) actions).dynamicCommand(channelId, command, durationMs);
    }

    @RuleAction(label = "@text/dynamics.action.label", description = "@text/dynamics.action.description")
    public void dynamicCommand(
            @ActionInput(name = "channelId", label = "@text/dynamics.channel.label", description = "@text/dynamics.channel.description") @Nullable String channelId,
            @ActionInput(name = "command", label = "@text/dynamics.command.label", description = "@text/dynamics.command.description") @Nullable Command command,
            @ActionInput(name = "durationMs", label = "@text/dynamics.duration.label", description = "@text/dynamics.duration.description") @Nullable Long durationMs) {
        //
        Clip2ThingHandler handler = this.handler;
        if (handler == null) {
            logger.warn("ThingHandler is null.");
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
        if (durationMs == null || durationMs.longValue() <= 0) {
            logger.debug("Duration is null, zero or negative.");
            return;
        }
        handler.handleDynamicsCommand(channelId, command,
                new QuantityType<>(durationMs.longValue(), MetricPrefix.MILLI(Units.SECOND)));
        logger.debug("Dynamic command '{}' sent to channelId '{}' with duration {}ms.", command, channelId, durationMs);
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
