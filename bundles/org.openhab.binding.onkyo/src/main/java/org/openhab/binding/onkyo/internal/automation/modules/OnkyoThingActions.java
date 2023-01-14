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
package org.openhab.binding.onkyo.internal.automation.modules;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onkyo.internal.handler.OnkyoHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some automation actions to be used with an {@link OnkyoThingActions}
 *
 * @author David Masshardt - initial contribution
 */
@ThingActionsScope(name = "onkyo")
@NonNullByDefault
public class OnkyoThingActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(OnkyoThingActions.class);

    private @Nullable OnkyoHandler handler;

    @SuppressWarnings("null")
    @RuleAction(label = "send a raw command", description = "Send a raw command to the receiver.")
    public void sendRawCommand(@ActionInput(name = "command") @Nullable String command,
            @ActionInput(name = "command") @Nullable String value) {
        logger.debug("sendRawCommand called with raw command: {} value: {}", command, value);
        if (handler == null) {
            logger.warn("Onkyo Action service ThingHandler is null!");
            return;
        }
        handler.sendRawCommand(command, value);
    }

    public static void sendRawCommand(ThingActions actions, @Nullable String command, @Nullable String value) {
        ((OnkyoThingActions) actions).sendRawCommand(command, value);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof OnkyoHandler) {
            this.handler = (OnkyoHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
