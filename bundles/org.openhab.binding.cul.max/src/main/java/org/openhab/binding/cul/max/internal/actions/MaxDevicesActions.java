/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul.max.internal.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.cul.max.internal.handler.MaxDevicesHandler;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MaxDevicesActions} class defines rule actions for MAX! devices
 *
 * @author Johannes Goehr (johgoe) - Initial contribution
 */
@ThingActionsScope(name = "maxcul")
@NonNullByDefault
public class MaxDevicesActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(MaxDevicesActions.class);

    private @Nullable MaxDevicesHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof MaxDevicesHandler) {
            this.handler = (MaxDevicesHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "reset the device", description = "Reset the device. Pairing is required again")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean resetDevice() {
        MaxDevicesHandler actionsHandler = handler;
        if (actionsHandler == null) {
            logger.info("MaxDevicesActions: Action service ThingHandler is null!");
            return false;
        }
        actionsHandler.resetDevice();
        return true;
    }

    public static boolean resetDevice(ThingActions actions) {
        return ((MaxDevicesActions) actions).resetDevice();
    }
}
