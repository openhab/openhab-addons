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
package org.openhab.binding.dirigera.internal.actions;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dirigera.internal.interfaces.DumpHandler;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * {@link DeviceActions} which can be performed by any device
 *
 * @author Bernd Weymann - Initial contribution
 */
@ThingActionsScope(name = "dirigera")
@NonNullByDefault
public class DeviceActions implements ThingActions {
    private Optional<DumpHandler> thingHandler = Optional.empty();

    @RuleAction(label = "@text/actionDumpLabel", description = "@text/actionDumpDescription")
    /**
     * Dump handler device JSON for debug purposes.
     */
    public void dump() {
        if (thingHandler.isPresent()) {
            thingHandler.get().dump();
        }
    }

    public static void dump(ThingActions actions) {
        ((DeviceActions) actions).dump();
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof DumpHandler dumpHandler) {
            thingHandler = Optional.of(dumpHandler);
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
