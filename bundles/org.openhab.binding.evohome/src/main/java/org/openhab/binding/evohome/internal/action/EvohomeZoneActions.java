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
import org.openhab.binding.evohome.internal.handler.EvohomeHeatingZoneHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Rule actions for permanent and temporary evohome heating zone overrides.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = EvohomeZoneActions.class)
@ThingActionsScope(name = "evohome")
@NonNullByDefault
public class EvohomeZoneActions implements ThingActions {

    private @Nullable EvohomeHeatingZoneHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = handler instanceof EvohomeHeatingZoneHandler zoneHandler ? zoneHandler : null;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/action.permanent.label", description = "@text/action.permanent.description")
    public @ActionOutput(type = "java.lang.Boolean", label = "@text/action.accepted.label", description = "@text/action.accepted.description") Boolean setPermanentSetPoint(
            @ActionInput(name = "temperature", label = "@text/action.temperature.label", description = "@text/action.temperature.description", required = true) @Nullable Double temperature) {
        EvohomeHeatingZoneHandler zoneHandler = handler;
        return temperature != null && zoneHandler != null && zoneHandler.setPermanentSetPoint(temperature);
    }

    @RuleAction(label = "@text/action.temporary.label", description = "@text/action.temporary.description")
    public @ActionOutput(type = "java.lang.Boolean", label = "@text/action.accepted.label", description = "@text/action.accepted.description") Boolean setTemporarySetPoint(
            @ActionInput(name = "temperature", label = "@text/action.temperature.label", description = "@text/action.temperature.description", required = true) @Nullable Double temperature,
            @ActionInput(name = "until", label = "@text/action.until.label", description = "@text/action.until.description", required = true) @Nullable ZonedDateTime until) {
        EvohomeHeatingZoneHandler zoneHandler = handler;
        return temperature != null && until != null && zoneHandler != null
                && zoneHandler.setTemporarySetPoint(temperature, until.toInstant());
    }

    @RuleAction(label = "@text/action.temporaryFor.label", description = "@text/action.temporaryFor.description")
    public @ActionOutput(type = "java.lang.Boolean", label = "@text/action.accepted.label", description = "@text/action.accepted.description") Boolean setTemporarySetPointFor(
            @ActionInput(name = "temperature", label = "@text/action.temperature.label", description = "@text/action.temperature.description", required = true) @Nullable Double temperature,
            @ActionInput(name = "minutes", label = "@text/action.minutes.label", description = "@text/action.minutes.description", required = true) @Nullable Integer minutes) {
        EvohomeHeatingZoneHandler zoneHandler = handler;
        return temperature != null && minutes != null && minutes >= 1 && minutes <= 1440 && zoneHandler != null
                && zoneHandler.setTemporarySetPoint(temperature, Instant.now().plus(minutes, ChronoUnit.MINUTES));
    }

    @RuleAction(label = "@text/action.nextSwitchpoint.label", description = "@text/action.nextSwitchpoint.description")
    public @ActionOutput(type = "java.lang.Boolean", label = "@text/action.accepted.label", description = "@text/action.accepted.description") Boolean setTemporarySetPointUntilNextSwitchpoint(
            @ActionInput(name = "temperature", label = "@text/action.temperature.label", description = "@text/action.temperature.description", required = true) @Nullable Double temperature) {
        EvohomeHeatingZoneHandler zoneHandler = handler;
        return temperature != null && zoneHandler != null
                && zoneHandler.setTemporarySetPointUntilNextSwitchpoint(temperature);
    }

    @RuleAction(label = "@text/action.cancel.label", description = "@text/action.cancel.description")
    public @ActionOutput(type = "java.lang.Boolean", label = "@text/action.accepted.label", description = "@text/action.accepted.description") Boolean cancelSetPointOverride() {
        EvohomeHeatingZoneHandler zoneHandler = handler;
        return zoneHandler != null && zoneHandler.cancelSetPointOverride();
    }

    public static Boolean setPermanentSetPoint(ThingActions actions, @Nullable Double temperature) {
        return toZoneActions(actions).setPermanentSetPoint(temperature);
    }

    public static Boolean setTemporarySetPoint(ThingActions actions, @Nullable Double temperature,
            @Nullable ZonedDateTime until) {
        return toZoneActions(actions).setTemporarySetPoint(temperature, until);
    }

    public static Boolean setTemporarySetPointFor(ThingActions actions, @Nullable Double temperature,
            @Nullable Integer minutes) {
        return toZoneActions(actions).setTemporarySetPointFor(temperature, minutes);
    }

    public static Boolean setTemporarySetPointUntilNextSwitchpoint(ThingActions actions, @Nullable Double temperature) {
        return toZoneActions(actions).setTemporarySetPointUntilNextSwitchpoint(temperature);
    }

    public static Boolean cancelSetPointOverride(ThingActions actions) {
        return toZoneActions(actions).cancelSetPointOverride();
    }

    private static EvohomeZoneActions toZoneActions(ThingActions actions) {
        if (actions instanceof EvohomeZoneActions zoneActions) {
            return zoneActions;
        }
        throw new IllegalArgumentException("Actions must belong to an evohome heating zone");
    }
}
