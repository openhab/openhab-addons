/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.avmfritz.internal.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.avmfritz.internal.handler.AVMFritzHeatingActionsHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * The {@link AVMFritzHeatingActions} defines thing actions for heating devices / groups of the avmfritz binding.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@ThingActionsScope(name = "avmfritz")
@NonNullByDefault
public class AVMFritzHeatingActions implements ThingActions {

    private @Nullable AVMFritzHeatingActionsHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (AVMFritzHeatingActionsHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/setBoostModeModeActionLabel", description = "@text/setBoostModeActionDescription")
    public void setBoostMode(
            @ActionInput(name = "Duration", label = "@text/setBoostModeDurationInputLabel", description = "@text/setBoostModeDurationInputDescription", type = "java.lang.Long", required = true) @Nullable Long duration) {
        AVMFritzHeatingActionsHandler actionsHandler = handler;
        if (actionsHandler == null) {
            throw new IllegalArgumentException("AVMFritzHeatingActions ThingHandler is null!");
        }
        if (duration == null) {
            throw new IllegalArgumentException("Cannot set Boost mode as 'duration' is null!");
        }
        actionsHandler.setBoostMode(duration.longValue());
    }

    public static void setBoostMode(ThingActions actions, @Nullable Long duration) {
        ((AVMFritzHeatingActions) actions).setBoostMode(duration);
    }

    @RuleAction(label = "@text/setWindowOpenModeActionLabel", description = "@text/setWindowOpenModeActionDescription")
    public void setWindowOpenMode(
            @ActionInput(name = "Duration", label = "@text/setWindowOpenModeDurationInputLabel", description = "@text/setWindowOpenModeDurationInputDescription", type = "java.lang.Long", required = true) @Nullable Long duration) {
        AVMFritzHeatingActionsHandler actionsHandler = handler;
        if (actionsHandler == null) {
            throw new IllegalArgumentException("AVMFritzHeatingActions ThingHandler is null!");
        }
        if (duration == null) {
            throw new IllegalArgumentException("Cannot set Window Open mode as 'duration' is null!");
        }
        actionsHandler.setWindowOpenMode(duration.longValue());
    }

    public static void setWindowOpenMode(ThingActions actions, @Nullable Long duration) {
        ((AVMFritzHeatingActions) actions).setWindowOpenMode(duration);
    }
}
