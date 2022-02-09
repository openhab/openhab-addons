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
package org.openhab.binding.smsmodem.internal.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smsmodem.internal.handler.SMSModemBridgeHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SMSModemActions} exposes some actions
 *
 * @author Gwendal ROULLEAU - Initial contribution
 */
@ThingActionsScope(name = "smsmodem")
@NonNullByDefault
public class SMSModemActions implements ThingActions {

    private @NonNullByDefault({}) SMSModemBridgeHandler handler;

    private final Logger logger = LoggerFactory.getLogger(SMSModemActions.class);

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (SMSModemBridgeHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "Send Message", description = "Send a message")
    public void send(
            @ActionInput(name = "recipient", label = "recipient", description = "Recipient of the message") @Nullable String recipient,
            @ActionInput(name = "message", label = "message", description = "Message to send") @Nullable String message) {
        if (recipient != null && !recipient.isEmpty() && message != null) {
            handler.send(recipient, message, false);
        } else {
            logger.error("SMSModem cannot send a message with no recipient or text");
        }
    }

    public static void send(@Nullable ThingActions actions, @Nullable String recipient, @Nullable String message) {
        if (actions instanceof SMSModemActions) {
            ((SMSModemActions) actions).send(recipient, message);
        } else {
            throw new IllegalArgumentException("Instance is not an SMSModemActions class.");
        }
    }
}
