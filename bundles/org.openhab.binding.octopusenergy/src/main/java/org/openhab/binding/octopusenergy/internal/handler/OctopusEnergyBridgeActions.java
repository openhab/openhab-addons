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
package org.openhab.binding.octopusenergy.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.octopusenergy.internal.exception.ApiException;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OctopusEnergyBridgeActions} class implements a actions for the Octopus Energy bridge.
 *
 * @author Rene Scherer - Initial contribution
 */
@ThingActionsScope(name = "octopusenergy")
@NonNullByDefault
public class OctopusEnergyBridgeActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(OctopusEnergyBridgeActions.class);

    private @Nullable OctopusEnergyBridgeHandler handler;

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = (OctopusEnergyBridgeHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "refresh", description = "Refreshes cached data from Octopus Energy.")
    public void refresh() {
        OctopusEnergyBridgeHandler handler = this.handler;
        if (handler != null) {
            try {
                handler.refresh();
            } catch (ApiException e) {
                logger.warn("Could not refresh data from API - {}", e.getMessage());
            }
        }
    }

    public static void refresh(@Nullable ThingActions actions) {
        if (actions instanceof OctopusEnergyBridgeActions) {
            ((OctopusEnergyBridgeActions) actions).refresh();
        } else {
            throw new IllegalArgumentException("Instance is not an OctopusEnergyElectricityMeterPointActions class.");
        }
    }
}
