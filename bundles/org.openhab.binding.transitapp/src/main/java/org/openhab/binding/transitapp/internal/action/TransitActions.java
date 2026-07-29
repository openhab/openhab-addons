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
package org.openhab.binding.transitapp.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.transitapp.internal.handler.TransitAppStopHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
@ThingActionsScope(name = "transitapp")
@Component(service = ThingActions.class, property = "thing.type.uid=transitapp:stop", scope = ServiceScope.PROTOTYPE)
public class TransitActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(TransitActions.class);

    private @Nullable ThingHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    @RuleAction(label = "Get Next Departure For Line", description = "Gets the next departure time for a specific line")
    public @Nullable String getNextDepartureForLine(@ActionInput(name = "lineName") String lineName) {
        ThingHandler currentHandler = this.handler;
        if (currentHandler instanceof TransitAppStopHandler) {
            return ((TransitAppStopHandler) currentHandler).findNextDepartureByLine(lineName);
        }
        logger.warn("Action getNextDepartureForLine called, but handler is not ready or not a Stop handler");
        return null;
    }
}
