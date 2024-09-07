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
package org.openhab.binding.pihole.internal;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openhab.binding.pihole.internal.PiHoleBindingConstants.BINDING_ID;

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@ThingActionsScope(name = BINDING_ID)
@NonNullByDefault
public class PiHoleActions implements ThingActions {
    private @Nullable PiHoleHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (PiHoleHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/action.disable.label", description = "@text/action.disable.description")
    public void disableBlocking(
            @ActionInput(name = "time", label = "@text/action.disable.timeLabel", description = "@text/action.disable.timeDescription") long time,
            @ActionInput(name = "timeUnit", label = "@text/action.disable.timeUnitLabel", description = "@text/action.disable.timeUnitDescription") @Nullable TimeUnit timeUnit)
            throws PiHoleException {
        if (time < 0) {
            return;
        }

        if (timeUnit == null) {
            timeUnit = SECONDS;
        }

        var local = handler;
        if (local == null) {
            return;
        }
        local.disableBlocking(timeUnit.toSeconds(time));
    }

    public static void disableBlocking(@Nullable ThingActions actions, long time, @Nullable TimeUnit timeUnit)
            throws PiHoleException {
        ((PiHoleActions) requireNonNull(actions)).disableBlocking(time, timeUnit);
    }

    @RuleAction(label = "@text/action.disable.label", description = "@text/action.disable.description")
    public void disableBlocking(
            @ActionInput(name = "time", label = "@text/action.disable.timeLabel", description = "@text/action.disable.timeDescription") long time)
            throws PiHoleException {
        disableBlocking(time, null);
    }

    public static void disableBlocking(@Nullable ThingActions actions, long time) throws PiHoleException {
        ((PiHoleActions) requireNonNull(actions)).disableBlocking(time);
    }

    @RuleAction(label = "@text/action.disableInf.label", description = "@text/action.disableInf.description")
    public void disableBlocking() throws PiHoleException {
        disableBlocking(0, null);
    }

    public static void disableBlocking(@Nullable ThingActions actions) throws PiHoleException {
        ((PiHoleActions) requireNonNull(actions)).disableBlocking(0);
    }

    @RuleAction(label = "@text/action.enable.label", description = "@text/action.enable.description")
    public void enableBlocking() throws PiHoleException {
        var local = handler;
        if (local == null) {
            return;
        }
        local.enableBlocking();
    }

    public static void enableBlocking(@Nullable ThingActions actions) throws PiHoleException {
        ((PiHoleActions) requireNonNull(actions)).enableBlocking();
    }
}
