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
package org.openhab.binding.yioremote.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * The {@link YIOremoteDockActions} is responsible for handling the action commands
 *
 * @author Michael Loercher - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = YIOremoteDockActions.class)
@ThingActionsScope(name = "yioremote")
@NonNullByDefault
public class YIOremoteDockActions implements ThingActions {
    private @Nullable YIOremoteDockHandler dockHandler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        dockHandler = handler instanceof YIOremoteDockHandler yioHandler ? yioHandler : null;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return dockHandler;
    }

    @RuleAction(label = "@text/action.sendIRCode.label", description = "@text/action.sendIRCode.description")
    public void sendIRCode(
            @ActionInput(name = "IRCode", label = "@text/action.sendIRCode.IRCode.label", description = "@text/action.sendIRCode.IRCode.description") @Nullable String irCode) {
        YIOremoteDockHandler handler = dockHandler;
        if (handler != null) {
            handler.sendIRCode(irCode);
        }
    }

    public static void sendIRCode(ThingActions actions, @Nullable String irCode) {
        ((YIOremoteDockActions) actions).sendIRCode(irCode);
    }
}
