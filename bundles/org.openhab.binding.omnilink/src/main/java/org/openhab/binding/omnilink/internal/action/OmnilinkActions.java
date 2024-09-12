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
package org.openhab.binding.omnilink.internal.action;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omnilink.internal.handler.OmnilinkBridgeHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the action handler service for the synchronizeControllerTime action.
 *
 * @author Ethan Dye - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = OmnilinkActions.class)
@ThingActionsScope(name = "omnilink")
@NonNullByDefault
public class OmnilinkActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(OmnilinkActions.class);
    public static Optional<TimeZoneProvider> timeZoneProvider = Optional.empty();
    private @Nullable OmnilinkBridgeHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof OmnilinkBridgeHandler bridgeHandler) {
            this.handler = bridgeHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/actionLabel", description = "@text/actionDesc")
    public void synchronizeControllerTime(
            @ActionInput(name = "zone", label = "@text/actionInputZoneLabel", description = "@text/actionInputZoneDesc") @Nullable String zone) {
        OmnilinkBridgeHandler actionsHandler = handler;
        if (actionsHandler == null) {
            logger.debug("Action service ThingHandler is null!");
        } else {
            ZonedDateTime zdt;
            if (ZoneId.getAvailableZoneIds().contains(zone)) {
                zdt = ZonedDateTime.now(ZoneId.of(zone));
            } else {
                logger.debug("Time zone provided invalid, using system default!");
                if (timeZoneProvider.isPresent()) {
                    zdt = ZonedDateTime.now(timeZoneProvider.get().getTimeZone());
                } else {
                    zdt = ZonedDateTime.now(ZoneId.systemDefault());
                }
            }
            actionsHandler.synchronizeControllerTime(zdt);
        }
    }

    public static void synchronizeControllerTime(ThingActions actions, @Nullable String zone) {
        ((OmnilinkActions) actions).synchronizeControllerTime(zone);
    }

    public static void setTimeZoneProvider(TimeZoneProvider tzp) {
        timeZoneProvider = Optional.of(tzp);
    }
}
