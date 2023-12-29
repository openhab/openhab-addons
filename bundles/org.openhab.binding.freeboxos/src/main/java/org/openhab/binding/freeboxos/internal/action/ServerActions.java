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
package org.openhab.binding.freeboxos.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.handler.ServerHandler;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {ServerActions} class is responsible to call corresponding actions on Freebox Server
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = ServerActions.class)
@ThingActionsScope(name = "freeboxos")
@NonNullByDefault
public class ServerActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(ServerActions.class);
    private @Nullable ServerHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof ServerHandler serverHandler) {
            this.handler = serverHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    @RuleAction(label = "reboot freebox server", description = "Reboots the Freebox Server")
    public void reboot() {
        logger.debug("Server reboot called");
        ServerHandler serverHandler = this.handler;
        if (serverHandler != null) {
            serverHandler.reboot();
        } else {
            logger.warn("Freebox Action service ThingHandler is null");
        }
    }
}
