/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.myenergi.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myenergi.internal.exception.ApiException;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MyenergiEddiActions} class implements actions on the MyEnergi eddi
 * energy diverter
 *
 * @author Stephen Cook - Initial contribution
 */
@ThingActionsScope(name = "myenergi")
@NonNullByDefault
public class MyenergiEddiActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(MyenergiEddiActions.class);

    private @Nullable MyenergiEddiHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (MyenergiEddiHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "setBoost", description = "Starts a heater boost.")
    public void setBoost(
            @ActionInput(name = "heater", label = "Heater", description = "The heater (1, 2) or relay id (11, 12).") int heater,
            @ActionInput(name = "duration", label = "Duration", description = "The boost duration in mins.") int duration) {
        try {
            logger.debug("setBoost({},{})", heater, duration);
            MyenergiEddiHandler h = handler;
            if (h != null) {
                logger.debug("calling setEddiBoost({},{},{})", h.serialNumber, heater, duration);
                MyenergiBridgeHandler bh = h.getBridgeHandler();
                if (bh == null) {
                    logger.warn("No bridge handler available");
                    throw new ApiException("No bridge handler available");
                }
                bh.setEddiBoost(h.serialNumber, heater, duration);
            }
        } catch (ApiException e) {
            logger.warn("Couldn't set boost - {}", e.getMessage());
        }
    }

    public static void setBoost(@Nullable ThingActions actions, int heater, int duration) {
        if (actions instanceof MyenergiEddiActions) {
            ((MyenergiEddiActions) actions).setBoost(heater, duration);
        } else {
            throw new IllegalArgumentException("Instance is not a MyEnergiEddiActions class.");
        }
    }

    @RuleAction(label = "cancelBoost", description = "Stops a heater boost.")
    public void cancelBoost(
            @ActionInput(name = "heater", label = "Heater", description = "The heater (1, 2) or relay id (11, 12).") int heater) {
        try {
            logger.debug("cancelBoost({})", heater);
            MyenergiEddiHandler h = handler;
            if (h != null) {
                logger.debug("calling cancelEddiBoost({},{})", h.serialNumber, heater);
                MyenergiBridgeHandler bh = h.getBridgeHandler();
                if (bh == null) {
                    logger.warn("No bridge handler available");
                    throw new ApiException("No bridge handler available");
                }
                bh.cancelEddiBoost(h.serialNumber, heater);
            }
        } catch (ApiException e) {
            logger.warn("Couldn't cancel boost - {}", e.getMessage());
        }
    }

    public static void cancelBoost(@Nullable ThingActions actions, int heater) {
        if (actions instanceof MyenergiEddiActions) {
            ((MyenergiEddiActions) actions).cancelBoost(heater);
        } else {
            throw new IllegalArgumentException("Instance is not a MyEnergiEddiActions class.");
        }
    }
}
