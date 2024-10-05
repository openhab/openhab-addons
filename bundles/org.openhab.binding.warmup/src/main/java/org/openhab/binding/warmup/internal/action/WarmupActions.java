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
package org.openhab.binding.warmup.internal.action;

import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.warmup.internal.handler.RoomHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author James Melville - Initial contribution
 */
@ThingActionsScope(name = "warmup")
@NonNullByDefault
public class WarmupActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(WarmupActions.class);

    private @Nullable RoomHandler handler;

    public WarmupActions() {
        logger.debug("Warmup action service instantiated");
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof RoomHandler roomHandler) {
            this.handler = roomHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "override", description = "Overrides the thermostat state for a specified time")
    public void setOverride(
            @ActionInput(name = "temperature", label = "Temperature", type = "QuantityType<Temperature>") @Nullable QuantityType<Temperature> temperature,
            @ActionInput(name = "duration", label = "Duration", type = "QuantityType<Time>") @Nullable QuantityType<Time> duration) {
        logger.debug("setOverride action called");
        RoomHandler handler = this.handler;
        if (handler != null && temperature != null && duration != null) {
            handler.setOverride(temperature, duration);
        } else {
            logger.warn("Warmup Action service argument is null!");
        }
    }

    public static void setOverride(@Nullable ThingActions actions, @Nullable QuantityType<Temperature> temperature,
            @Nullable QuantityType<Time> duration) {
        if (actions instanceof WarmupActions warmupActions) {
            warmupActions.setOverride(temperature, duration);
        } else {
            throw new IllegalArgumentException("Instance is not a WarmupActions class.");
        }
    }
}
