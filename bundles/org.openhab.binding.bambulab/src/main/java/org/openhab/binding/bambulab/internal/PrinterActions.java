/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.bambulab.internal;

import static java.util.Objects.requireNonNull;
import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.BINDING_ID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@ThingActionsScope(name = BINDING_ID)
@NonNullByDefault
public class PrinterActions implements ThingActions {
    private @Nullable PrinterHandler handler;

    @Override
    public void setThingHandler(ThingHandler thingHandler) {
        this.handler = (PrinterHandler) thingHandler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/action.sendCommand.label", description = "@text/action.sendCommand.description")
    public void sendCommand(
            @ActionInput(name = "time", label = "@text/action.sendCommand.commandLabel", description = "@text/action.sendCommand.commandDescription") String stringCommand) {
        var localHandler = handler;
        if (localHandler == null) {
            return;
        }

        localHandler.sendCommand(stringCommand);
    }

    public static void sendCommand(@Nullable ThingActions actions, String stringCommand) {
        ((PrinterActions) requireNonNull(actions)).sendCommand(stringCommand);
    }

    @RuleAction(label = "@text/action.refreshChannels.label", description = "@text/action.refreshChannels.description")
    public void refreshChannels() {
        var localHandler = handler;
        if (localHandler == null) {
            return;
        }

        localHandler.refreshChannels();
    }

    public static void refreshChannels(@Nullable ThingActions actions) {
        ((PrinterActions) requireNonNull(actions)).refreshChannels();
    }
}
