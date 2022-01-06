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
package org.openhab.binding.caddx.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.caddx.internal.handler.ThingHandlerZone;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the automation engine action handler service for the
 * caddx bridge actions.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@ThingActionsScope(name = "caddx")
@NonNullByDefault
public class CaddxZoneActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(CaddxZoneActions.class);

    private static final String HANDLER_IS_NULL = "ThingHandlerZone is null!";

    private @Nullable ThingHandlerZone handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof ThingHandlerZone) {
            this.handler = (ThingHandlerZone) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    @RuleAction(label = "bypass", description = "Bypass the zone")
    public void bypass() {
        // Check of parameters
        ThingHandlerZone thingHandler = this.handler;
        if (thingHandler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        thingHandler.bypass();
    }

    public static void bypass(ThingActions actions) {
        ((CaddxZoneActions) actions).bypass();
    }
}
