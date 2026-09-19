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
package org.openhab.binding.autoblind.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.autoblind.internal.handler.AutoBlindHubHandler;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Thing Actions for the AutoBlind Hub.
 *
 * @author Stephen Berg - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = AutoBlindHubActions.class)
@ThingActionsScope(name = "autoblind")
@NonNullByDefault
public class AutoBlindHubActions implements ThingActions {

    private @Nullable AutoBlindHubHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (AutoBlindHubHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "force refresh", description = "Clear command suppression and poll the hub for current shade positions")
    public void forceRefresh() {
        AutoBlindHubHandler localHandler = handler;
        if (localHandler != null) {
            localHandler.forceRefresh();
        }
    }

    public static void forceRefresh(@Nullable ThingActions actions) {
        if (actions instanceof AutoBlindHubActions hubActions) {
            hubActions.forceRefresh();
        } else {
            throw new IllegalArgumentException("Instance is not an AutoBlindHubActions class.");
        }
    }
}
