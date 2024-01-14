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
package org.openhab.binding.prowl.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.prowl.internal.ProwlHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ProwlActions} class contains methods for use in DSL.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = ProwlActions.class)
@ThingActionsScope(name = "prowl")
@NonNullByDefault
public class ProwlActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(ProwlActions.class);
    private @Nullable ProwlHandler handler;

    @Override
    public void setThingHandler(ThingHandler thingHandler) {
        this.handler = (ProwlHandler) thingHandler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/pushNotificationActionLabel", description = "@text/pushNotificationActionDescription")
    public void pushNotification(
            @ActionInput(name = "event", label = "@text/pushNotificationActionEventLabel", description = "@text/pushNotificationActionEventDescription") @Nullable String event,
            @ActionInput(name = "message", label = "@text/pushNotificationActionMessageLabel", description = "@text/pushNotificationActionMessageDescription") @Nullable String message) {
        ProwlHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("Prowl ThingHandler is null");
            return;
        }

        handler.pushNotification(event, message);
    }

    @RuleAction(label = "@text/pushNotificationActionLabel", description = "@text/pushNotificationActionDescription")
    public void pushNotification(
            @ActionInput(name = "event", label = "@text/pushNotificationActionEventLabel", description = "@text/pushNotificationActionEventDescription") @Nullable String event,
            @ActionInput(name = "message", label = "@text/pushNotificationActionMessageLabel", description = "@text/pushNotificationActionMessageDescription") @Nullable String message,
            @ActionInput(name = "priority", label = "@text/pushNotificationActionPriorityLabel", description = "@text/pushNotificationActionPriorityDescription") int priority) {
        ProwlHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("Prowl ThingHandler is null");
            return;
        }

        handler.pushNotification(event, message, priority);
    }

    public static void pushNotification(@Nullable ThingActions actions, @Nullable String event,
            @Nullable String description) {
        pushNotification(actions, event, description, 0);
    }

    public static void pushNotification(@Nullable ThingActions actions, @Nullable String event,
            @Nullable String description, int priority) {
        if (actions instanceof ProwlActions prowlActions) {
            prowlActions.pushNotification(event, description, priority);
        } else {
            throw new IllegalArgumentException("Instance is not a ProwlActions class.");
        }
    }
}
