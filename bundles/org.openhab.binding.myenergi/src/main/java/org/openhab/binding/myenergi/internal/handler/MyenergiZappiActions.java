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
import org.openhab.binding.myenergi.internal.model.ZappiChargingMode;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MyenergiZappiActions} class implements actions on the Zappi EV
 * charger
 *
 * @author Rene Scherer - Initial contribution
 */
@ThingActionsScope(name = "myenergi")
@NonNullByDefault
public class MyenergiZappiActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(MyenergiZappiActions.class);

    private @Nullable MyenergiZappiHandler handler;

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = (MyenergiZappiHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "setChargingMode", description = "Sets the Zappi charging mode.")
    public void setChargingMode(
            @ActionInput(name = "chargingMode", label = "Charging Mode", description = "The new mode (BOOST, FAST, ECO, ECO+, STOP).") ZappiChargingMode chargingMode) {
        try {
            logger.debug("setChargingMode({})", chargingMode);
            MyenergiZappiHandler h = handler;
            if (h != null) {
                logger.debug("calling setChargingMode({},{})", h.serialNumber, chargingMode);
                MyenergiBridgeHandler bh = h.getBridgeHandler();
                if (bh == null) {
                    logger.warn("No bridge handler available");
                    throw new ApiException("No bridge handler available");
                }
                bh.setZappiChargingMode(h.serialNumber, chargingMode);
            }
        } catch (ApiException e) {
            logger.warn("Couldn't set boost - {}", e.getMessage());
        }
    }

    public static void setChargingMode(@Nullable ThingActions actions, ZappiChargingMode chargingMode) {
        if (actions instanceof MyenergiZappiActions) {
            ((MyenergiZappiActions) actions).setChargingMode(chargingMode);
        } else {
            throw new IllegalArgumentException("Instance is not a MyEnergiZappiActions class.");
        }
    }
}
