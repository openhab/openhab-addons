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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;


@ThingActionsScope(name = "freemobilesms") // Your bindings id is usually the scope
@NonNullByDefault
public class FreeMobileSmsActions implements ThingActions {
    private @Nullable FreeMobileSmsHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) { this.handler = (FreeMobileSmsHandler) handler; }

    @Override
    public @Nullable ThingHandler getThingHandler() { return handler; }

    @RuleAction(label = "@text/actionLabel", description = "@text/actionDesc")
    public void sendMessage(
            @ActionInput(name = "message", label = "@text/actionInputMessageLabel", description = "@text/actionInputMessageDesc") @Nullable String message) {
        this.handler.sendMessage(message);
    }

    public static void sendMessage(@Nullable ThingActions actions, @Nullable String message) {
        if (actions instanceof FreeMobileSmsActions) {
            ((FreeMobileSmsActions) actions).sendMessage(message);
        } else {
            throw new IllegalArgumentException("Instance is not an FreeMobileSmsActions class.");
        }
    }

}
