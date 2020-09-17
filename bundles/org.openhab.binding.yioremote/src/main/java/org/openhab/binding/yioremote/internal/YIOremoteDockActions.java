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
package org.openhab.binding.yioremote.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

/**
 * The {@link YIOremoteDockActions} is responsible for handling the action commands
 *
 *
 * @author Michael Loercher - Initial contribution
 */
@ThingActionsScope(name = "yioremote")
@NonNullByDefault
public class YIOremoteDockActions implements ThingActions {
    private @Nullable static YIOremoteDockHandler dockHandler;

    @Override
    public void setThingHandler(@Nullable ThingHandler yiremotedockhandler) {
        dockHandler = (YIOremoteDockHandler) yiremotedockhandler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return dockHandler;
    }

    public static void sendircode(@Nullable ThingActions actions, @Nullable String irCode) {
        if (actions instanceof YIOremoteDockActions && dockHandler != null) {
            switch (dockHandler.getyioRemoteDockActualStatus()) {
                case AUTHENTICATION_COMPLETE:
                    dockHandler.sendIRCode(irCode);
                    break;
            }
        } else {
            throw new IllegalArgumentException("Instance is not an YIOremoteDockActions class.");
        }
    }
}
