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
package org.openhab.binding.dirigera.internal.actions;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dirigera.internal.interfaces.DebugHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * {@link DebugActions} which can be performed by any device
 *
 * @author Bernd Weymann - Initial contribution
 */
@ThingActionsScope(name = "dirigera")
@NonNullByDefault
public class DebugActions implements ThingActions {
    private Optional<DebugHandler> thingHandler = Optional.empty();

    /**
     * Get handler device JSON for debug purposes.
     */
    @RuleAction(label = "@text/actionGetJsonLabel", description = "@text/actionGetJsonDescription")
    public @ActionOutput(name = "result", label = "Device JSON", type = "java.lang.String") String getJSON() {
        if (thingHandler.isPresent()) {
            return thingHandler.get().getJSON();
        }
        return "{}";
    }

    public static String getJSON(ThingActions actions) {
        return ((DebugActions) actions).getJSON();
    }

    /**
     * Get gateway token from everywhere. No need to call it directly from gateway.
     */
    @RuleAction(label = "@text/actionGetTokenLabel", description = "@text/actionGetTokenDescription")
    public @ActionOutput(name = "result", label = "Token", type = "java.lang.String") String getToken() {
        if (thingHandler.isPresent()) {
            return thingHandler.get().getToken();
        }
        return "no token available";
    }

    public static String getToken(ThingActions actions) {
        return ((DebugActions) actions).getToken();
    }

    /**
     * Set specific device into debug mode showing commands and updates on trace info level
     */
    @RuleAction(label = "@text/actionSetDebugLabel", description = "@text/actionSetDebugDescription")
    public void setDebug(
            @ActionInput(name = "debug", label = "@text/actionDebugFlagLabel", description = "@text/actionDebugFlagDescription") boolean debug,
            @ActionInput(name = "all", label = "@text/actionDebugFlagForAllLabel", description = "@text/actionDebugFlagForAllDescription") boolean all) {
        if (thingHandler.isPresent()) {
            thingHandler.get().setDebug(debug, all);
        }
    }

    public static void setDebug(ThingActions actions, boolean debug, boolean all) {
        ((DebugActions) actions).setDebug(debug, all);
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof DebugHandler debugHandler) {
            thingHandler = Optional.of(debugHandler);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        if (thingHandler.isPresent()) {
            return thingHandler.get();
        }
        return null;
    }
}
