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
import org.openhab.binding.caddx.internal.handler.ThingHandlerKeypad;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
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
public class CaddxKeypadActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(CaddxKeypadActions.class);

    private static final String HANDLER_IS_NULL = "ThingHandlerKeypad is null!";
    private static final String ACTION_CLASS_IS_WRONG = "Instance is not a CaddxKeypadActions class.";
    private static final String TEXT_IS_NULL = "The value for the text is null. Action not executed.";
    private static final String DISPLAY_LOCATION_IS_NULL = "The value for the display location is null. Action not executed.";
    private static final String DISPLAY_LOCATION_IS_INVALID = "The value for the display location [{}] is invalid. Action not executed.";

    private @Nullable ThingHandlerKeypad handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (ThingHandlerKeypad) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    @RuleAction(label = "enterTerminalMode", description = "Enter terminal mode on the selected keypad")
    public void enterTerminalMode() throws IOException {
        ThingHandlerKeypad thingHandler = this.handler;
        if (thingHandler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        thingHandler.enterTerminalMode();
    }

    @RuleAction(label = "enterTerminalMode", description = "Enter terminal mode on the selected keypad")
    public static void enterTerminalMode(@Nullable ThingActions actions) throws IOException {
        if (actions instanceof CaddxKeypadActions) {
            ((CaddxKeypadActions) actions).enterTerminalMode();
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "sendKeypadTextMessage", description = "Display a message on the Keypad")
    public void sendKeypadTextMessage(
            @ActionInput(name = "displayLocation", label = "Display Location", description = "Display storage location (0=top left corner)") @Nullable String displayLocation,
            @ActionInput(name = "text", label = "Text", description = "The text to be displayed") @Nullable String text)
            throws IOException {
        ThingHandlerKeypad thingHandler = handler;
        if (thingHandler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        if (text == null) {
            logger.debug(TEXT_IS_NULL);
            return;
        }

        if (displayLocation == null) {
            logger.debug(DISPLAY_LOCATION_IS_NULL);
            return;
        }

        if (!displayLocation.matches("^\\d$")) {
            logger.debug(DISPLAY_LOCATION_IS_INVALID, displayLocation);
            return;
        }

        // Adjust parameters
        String paddedText = text + "        ";
        paddedText = paddedText.substring(0, 8);

        // Build the command
        thingHandler.sendKeypadTextMessage(displayLocation, text);
    }

    @RuleAction(label = "sendKeypadTextMessage", description = "Display a message on the Keypad")
    public static void sendKeypadTextMessage(@Nullable ThingActions actions, @Nullable String displayLocation,
            @Nullable String text) throws IOException {
        if (actions instanceof CaddxKeypadActions) {
            ((CaddxKeypadActions) actions).sendKeypadTextMessage(displayLocation, text);
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }
}
