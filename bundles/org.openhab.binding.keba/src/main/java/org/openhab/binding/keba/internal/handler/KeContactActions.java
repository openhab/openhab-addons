/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.keba.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.keba.internal.KebaBindingConstants;
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
 * The {@link KeContactActions} is responsible for handling actions, which
 * are sent to the binding.
 *
 * @author Simon Spielmann - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = KeContactActions.class)
@ThingActionsScope(name = KebaBindingConstants.BINDING_ID)
@NonNullByDefault
public class KeContactActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(KeContactActions.class);
    private @Nullable KeContactHandler handler;

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = (KeContactHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/actionLabel", description = "@text/actionDesc")
    public void setDisplay(
            @ActionInput(name = "text", label = "@text/actionInputTextLabel", description = "@text/actionInputTextDesc") @Nullable String text,
            @ActionInput(name = "durationMin", label = "@text/actionInputDurationMinLabel", description = "@text/actionInputDurationMinDesc") int durationMin,
            @ActionInput(name = "durationMax", label = "@text/actionInputDurationMaxLabel", description = "@text/actionInputDurationMaxDesc") int durationMax) {
        if (handler == null) {
            logger.warn("KeContact Action service ThingHandler is null!");
            return;
        }
        handler.setDisplay(text, durationMin, durationMax);
    }

    public static void setDisplay(ThingActions actions, @Nullable String text, int durationMin, int durationMax) {
        ((KeContactActions) actions).setDisplay(text, durationMin, durationMax);
    }

    @RuleAction(label = "@text/actionLabel", description = "@text/actionDesc")
    public void setDisplay(
            @ActionInput(name = "text", label = "@text/actionInputTextLabel", description = "@text/actionInputTextDesc") @Nullable String text) {
        if (handler == null) {
            logger.warn("KeContact Action service ThingHandler is null!");
            return;
        }
        handler.setDisplay(text, -1, -1);
    }

    public static void setDisplay(ThingActions actions, @Nullable String text) {
        ((KeContactActions) actions).setDisplay(text);
    }
}
