/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.freemobilesms.internal;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ThingActionsScope(name = "freemobilesms") // Your bindings id is usually the scope
@NonNullByDefault
public class FreeMobileSmsActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(FreeMobileSmsActions.class);

    private @Nullable FreeMobileSmsHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof FreeMobileSmsHandler) {
            this.handler = (FreeMobileSmsHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() { return handler; }

    @RuleAction(label = "@text/actionLabel", description = "@text/actionDesc")
    public void sendFreeMobileSMS(
            @ActionInput(name = "message", label = "@text/actionInputMessageLabel", description = "@text/actionInputMessageDesc") @NonNull String message) {
        if (this.handler == null)
            logger.error("Handler is null!");
        else
            this.handler.sendMessage(message);
    }

    public static void sendFreeMobileSMS(@Nullable ThingActions actions, @NonNull String message) {
        if (actions instanceof FreeMobileSmsActions) {
            ((FreeMobileSmsActions) actions).sendFreeMobileSMS(message);
        } else {
            throw new IllegalArgumentException("Instance is not an FreeMobileSmsActions class.");
        }
    }

}
