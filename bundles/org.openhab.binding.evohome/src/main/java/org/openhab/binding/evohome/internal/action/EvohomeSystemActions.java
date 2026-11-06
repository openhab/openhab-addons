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
package org.openhab.binding.evohome.internal.action;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.evohome.internal.handler.EvohomeTemperatureControlSystemHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Rule actions for permanent and temporary evohome system modes.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = EvohomeSystemActions.class)
@ThingActionsScope(name = "evohome")
@NonNullByDefault
public class EvohomeSystemActions implements ThingActions {

    private @Nullable EvohomeTemperatureControlSystemHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = handler instanceof EvohomeTemperatureControlSystemHandler systemHandler ? systemHandler : null;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/action.systemMode.label", description = "@text/action.systemMode.description")
    public @ActionOutput(type = "java.lang.Boolean", label = "@text/action.accepted.label", description = "@text/action.accepted.description") Boolean setSystemMode(
            @ActionInput(name = "mode", label = "@text/action.mode.label", description = "@text/action.mode.description", required = true) @Nullable String mode) {
        EvohomeTemperatureControlSystemHandler systemHandler = handler;
        return mode != null && !mode.isBlank() && systemHandler != null && systemHandler.setSystemMode(mode);
    }

    @RuleAction(label = "@text/action.temporarySystemMode.label", description = "@text/action.temporarySystemMode.description")
    public @ActionOutput(type = "java.lang.Boolean", label = "@text/action.accepted.label", description = "@text/action.accepted.description") Boolean setTemporarySystemMode(
            @ActionInput(name = "mode", label = "@text/action.mode.label", description = "@text/action.mode.description", required = true) @Nullable String mode,
            @ActionInput(name = "until", label = "@text/action.until.label", description = "@text/action.until.description", required = true) @Nullable ZonedDateTime until) {
        EvohomeTemperatureControlSystemHandler systemHandler = handler;
        return mode != null && !mode.isBlank() && until != null && systemHandler != null
                && systemHandler.setTemporarySystemMode(mode, until.toInstant());
    }

    @RuleAction(label = "@text/action.temporarySystemModeFor.label", description = "@text/action.temporarySystemModeFor.description")
    public @ActionOutput(type = "java.lang.Boolean", label = "@text/action.accepted.label", description = "@text/action.accepted.description") Boolean setTemporarySystemModeFor(
            @ActionInput(name = "mode", label = "@text/action.mode.label", description = "@text/action.mode.description", required = true) @Nullable String mode,
            @ActionInput(name = "minutes", label = "@text/action.systemMinutes.label", description = "@text/action.systemMinutes.description", required = true) @Nullable Integer minutes) {
        EvohomeTemperatureControlSystemHandler systemHandler = handler;
        return mode != null && !mode.isBlank() && minutes != null && minutes > 0 && systemHandler != null
                && systemHandler.setTemporarySystemMode(mode, Instant.now().plus(minutes, ChronoUnit.MINUTES));
    }

    public static Boolean setSystemMode(ThingActions actions, @Nullable String mode) {
        return toSystemActions(actions).setSystemMode(mode);
    }

    public static Boolean setTemporarySystemMode(ThingActions actions, @Nullable String mode,
            @Nullable ZonedDateTime until) {
        return toSystemActions(actions).setTemporarySystemMode(mode, until);
    }

    public static Boolean setTemporarySystemModeFor(ThingActions actions, @Nullable String mode,
            @Nullable Integer minutes) {
        return toSystemActions(actions).setTemporarySystemModeFor(mode, minutes);
    }

    private static EvohomeSystemActions toSystemActions(ThingActions actions) {
        if (actions instanceof EvohomeSystemActions systemActions) {
            return systemActions;
        }
        throw new IllegalArgumentException("Actions must belong to an evohome control display");
    }
}
