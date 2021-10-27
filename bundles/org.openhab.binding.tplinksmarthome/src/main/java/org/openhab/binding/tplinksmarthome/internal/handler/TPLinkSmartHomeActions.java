/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.tplinksmarthome.internal.handler;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;

/**
 * TP-Link Smart Home Rule Actions.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@ThingActionsScope(name = "tplinksmarthome")
@NonNullByDefault
public class TPLinkSmartHomeActions implements ThingActions, ThingHandlerService {

    private @Nullable ThingHandler handler;

    @RuleAction(label = "@text/actions.send.label", description = "@text/actions.send.description")
    public String send(
            @ActionInput(name = "command", label = "@text/actions.send.command.label", description = "@text/actions.send.command.description", type = "java.lang.String", required = true) final String command)
            throws IOException {
        return ((SmartHomeHandler) getThingHandler()).getConnection().sendCommand(command);
    }

    public static void send(final ThingActions actions, final String command) throws IOException {
        ((TPLinkSmartHomeActions) actions).send(command);
    }

    @Override
    public void setThingHandler(final ThingHandler handler) {
        this.handler = handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
