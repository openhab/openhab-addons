/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.lghorizon.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lghorizon.internal.handler.LGHorizonBoxHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Thing Actions for a {@code box}: displays a short on-screen message. {@code duration} is
 * optional; when omitted, {@link LGHorizonBoxHandler#displayMessage} falls back to
 * {@link org.openhab.binding.lghorizon.internal.LGHorizonBindingConstants#DEFAULT_DISPLAY_MESSAGE_DURATION_SECONDS}.
 *
 * @author Mark Herwege - Initial contribution
 */
@ThingActionsScope(name = "lghorizon")
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = LGHorizonActions.class)
public class LGHorizonActions implements ThingActions {

    private @Nullable LGHorizonBoxHandler handler;

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = handler instanceof LGHorizonBoxHandler boxHandler ? boxHandler : null;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "Display Message", description = "Shows a short text message as an on-screen overlay on the TV.")
    public void displayMessage(
            @ActionInput(name = "message", type = "java.lang.String", label = "Message", description = "The text to display.", required = true) String message,
            @ActionInput(name = "duration", type = "java.lang.Integer", label = "Duration (s)", description = "Optional duration in seconds the message stays visible. Defaults to 10s if not given.") @Nullable Integer duration) {
        LGHorizonBoxHandler boxHandler = handler;
        if (boxHandler == null) {
            return;
        }
        boxHandler.displayMessage(message, duration);
    }

    /**
     * Display message on screen. Static delegate for use from the Rules DSL / scripted automation.
     *
     * @param actions
     * @param message
     */
    public static void displayMessage(ThingActions actions, String message) {
        displayMessage(actions, message, null);
    }

    /**
     * Display message on screen. Static delegate for use from the Rules DSL / scripted automation.
     *
     * @param actions
     * @param message
     * @param duration
     */
    public static void displayMessage(ThingActions actions, String message, @Nullable Integer duration) {
        ((LGHorizonActions) actions).displayMessage(message, duration);
    }
}
