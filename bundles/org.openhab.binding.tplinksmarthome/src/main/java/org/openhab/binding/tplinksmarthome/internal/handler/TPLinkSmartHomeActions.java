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
package org.openhab.binding.tplinksmarthome.internal.handler;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TP-Link Smart Home Rule Actions.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@ThingActionsScope(name = "tplinksmarthome")
@NonNullByDefault
public class TPLinkSmartHomeActions implements ThingActions, ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(TPLinkSmartHomeActions.class);

    private @Nullable SmartHomeHandler handler;

    @RuleAction(label = "@text/actions.tplinksmarthome.send.label", description = "@text/actions.tplinksmarthome.send.description")
    @ActionOutput(name = "response", label = "@text/actions.tplinksmarthome.send.response.label", description = "@text/actions.tplinksmarthome.send.response.description", type = "java.lang.String")
    public String send(
            @ActionInput(name = "command", label = "@text/actions.tplinksmarthome.send.command.label", description = "@text/actions.tplinksmarthome.send.command.description", type = "java.lang.String", required = true) final String command)
            throws IOException {
        if (handler instanceof SmartHomeHandler) {
            return handler.getConnection().sendCommand(command);
        } else {
            logger.warn("Could not send command to tplink device because handler not set.");
            return "";
        }
    }

    public static String send(final ThingActions actions, final String command) throws IOException {
        return ((TPLinkSmartHomeActions) actions).send(command);
    }

    @Override
    public void setThingHandler(final ThingHandler handler) {
        if (handler instanceof SmartHomeHandler smartHomeHandler) {
            this.handler = smartHomeHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
