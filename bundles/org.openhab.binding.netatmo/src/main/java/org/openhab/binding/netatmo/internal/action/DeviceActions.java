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
package org.openhab.binding.netatmo.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.handler.NetatmoDeviceHandler;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DeviceActions} defines thing actions for each Netatmo thing.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@ThingActionsScope(name = "netatmo")
@NonNullByDefault
public class DeviceActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(DeviceActions.class);

    private @Nullable NetatmoDeviceHandler handler;

    public DeviceActions() {
        logger.trace("Netatmo Device actions service created");
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof NetatmoDeviceHandler) {
            this.handler = (NetatmoDeviceHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/reconnectApiLabel", description = "@text/reconnectApiDesc")
    public void reconnectApi() {
        NetatmoDeviceHandler roomHandler = handler;
        if (roomHandler == null) {
            logger.debug("Handler not set for device thing actions.");
            return;
        }

        roomHandler.reconnectApi();
    }

    /**
     * Static setLevel method for Rules DSL backward compatibility
     */
    public static void reconnectApi(ThingActions actions) {
        ((DeviceActions) actions).reconnectApi();
    }
}
