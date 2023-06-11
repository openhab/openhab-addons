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
package org.openhab.binding.yioremote.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * The {@link YIOremoteDockActions} is responsible for handling the action commands
 *
 *
 * @author Michael Loercher - Initial contribution
 */
@ThingActionsScope(name = "yioremote")
@NonNullByDefault
public class YIOremoteDockActions implements ThingActions {
    private @Nullable YIOremoteDockHandler dockHandler;

    @Override
    public void setThingHandler(@Nullable ThingHandler yiremotedockhandler) {
        dockHandler = (YIOremoteDockHandler) yiremotedockhandler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return dockHandler;
    }

    @RuleAction(label = "@text/actionLabel", description = "@text/actionDesc")
    public void sendIRCode(
            @ActionInput(name = "IRCode", label = "@text/actionInputTopicLabel", description = "@text/actionInputTopicDesc") @Nullable String irCode) {
        YIOremoteDockHandler dockHandlerLocal = dockHandler;
        if (dockHandlerLocal != null) {
            dockHandlerLocal.sendIRCode(irCode);
        }
    }

    public static void sendIRCode(ThingActions actions, @Nullable String irCode) {
        ((YIOremoteDockActions) actions).sendIRCode(irCode);
    }
}
