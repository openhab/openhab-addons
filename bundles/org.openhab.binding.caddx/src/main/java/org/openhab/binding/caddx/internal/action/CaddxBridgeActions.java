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
package org.openhab.binding.caddx.internal.action;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.caddx.internal.handler.CaddxBridgeHandler;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the automation engine action handler service for the
 * caddx bridge actions.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@ThingActionsScope(name = "caddx")
@NonNullByDefault
public class CaddxBridgeActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(CaddxBridgeActions.class);

    private static final String HANDLER_IS_NULL = "CaddxBridgeHandler is null!";
    private static final String ACTION_CLASS_IS_WRONG = "Instance is not a CaddxBridgeActions class.";
    private @Nullable CaddxBridgeHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (CaddxBridgeHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    @RuleAction(label = "restart", description = "Restart the binding")
    public void restart() throws IOException {
        // Check of parameters
        CaddxBridgeHandler handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.restart();
    }

    @RuleAction(label = "restart", description = "Restart the binding")
    public static void restart(@Nullable ThingActions actions) throws IOException {
        if (actions instanceof CaddxBridgeActions) {
            ((CaddxBridgeActions) actions).restart();
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }
}
