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
package org.openhab.binding.norman.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.norman.internal.handler.NormanHubHandler;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Thing Actions for the Norman Hub.
 *
 * @author Stephen Berg - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = NormanHubActions.class)
@ThingActionsScope(name = "norman")
@NonNullByDefault
public class NormanHubActions implements ThingActions {

    private @Nullable NormanHubHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (NormanHubHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/action.forceRefresh.label", description = "@text/action.forceRefresh.description")
    public void forceRefresh() {
        NormanHubHandler localHandler = handler;
        if (localHandler != null) {
            localHandler.forceRefresh();
        }
    }

    public static void forceRefresh(@Nullable ThingActions actions) {
        if (actions instanceof NormanHubActions hubActions) {
            hubActions.forceRefresh();
        } else {
            throw new IllegalArgumentException("Instance is not a NormanHubActions class.");
        }
    }
}
