/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.omnilink.internal.action;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omnilink.internal.handler.OmnilinkBridgeHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the action handler service for the set_date action.
 *
 * @author Ethan Dye - Initial contribution
 */
@ThingActionsScope(name = "omnilink")
@NonNullByDefault
public class OmnilinkActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(OmnilinkActions.class);
    private final TimeZoneProvider timeZoneProvider;
    private @Nullable OmnilinkBridgeHandler handler;

    public OmnilinkActions(final @Reference TimeZoneProvider timeZoneProvider) {
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof OmnilinkBridgeHandler) {
            this.handler = (OmnilinkBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/actionLabel", description = "@text/actionDesc")
    public void setDateTime(
            @ActionInput(name = "zone", label = "@text/actionInputZoneLabel", description = "@text/actionInputZoneDesc") @Nullable String zone) {
        OmnilinkBridgeHandler actionsHandler = handler;
        ZonedDateTime zdt;
        if (zone != null) {
            zdt = ZonedDateTime.now(ZoneId.of(zone));
        } else {
            zdt = ZonedDateTime.now(timeZoneProvider.getTimeZone());
        }
        if (actionsHandler == null) {
            logger.info("Action service ThingHandler is null!");
        } else {
            actionsHandler.setDateTime(zdt);
        }
    }
}
