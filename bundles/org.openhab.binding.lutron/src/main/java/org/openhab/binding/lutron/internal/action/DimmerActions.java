/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.action;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.handler.DimmerHandler;
import org.openhab.binding.lutron.internal.protocol.LutronDuration;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DimmerActions} defines thing actions for DimmerHandler.
 *
 * @author Bob Adair - Initial contribution
 */
@ThingActionsScope(name = "lutron")
@NonNullByDefault
public class DimmerActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(DimmerActions.class);

    private @Nullable DimmerHandler handler;

    public DimmerActions() {
        logger.trace("Lutron Dimmer actions service created");
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof DimmerHandler dimmerHandler) {
            this.handler = dimmerHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    /**
     * The setLevel dimmer thing action
     */
    @RuleAction(label = "send a set level command", description = "Send set level command with fade and delay times.")
    public void setLevel(
            @ActionInput(name = "level", label = "Dimmer Level", description = "New dimmer level (0-100)") @Nullable Double level,
            @ActionInput(name = "fadeTime", label = "Fade Time", description = "Time to fade to new level (seconds)") @Nullable Double fadeTime,
            @ActionInput(name = "delayTime", label = "Delay Time", description = "Delay before starting fade (seconds)") @Nullable Double delayTime) {
        DimmerHandler dimmerHandler = handler;
        if (dimmerHandler == null) {
            logger.debug("Handler not set for Dimmer thing actions.");
            return;
        }
        if (level == null) {
            logger.debug("Ignoring setLevel command due to null level value.");
            return;
        }
        if (fadeTime == null) {
            logger.debug("Ignoring setLevel command due to null value for fadeTime.");
            return;
        }
        if (delayTime == null) {
            logger.debug("Ignoring setLevel command due to null value for delayTime.");
            return;
        }

        Double lightLevel = level;
        if (lightLevel > 100.0) {
            lightLevel = 100.0;
        } else if (lightLevel < 0.0) {
            lightLevel = 0.0;
        }
        try {
            dimmerHandler.setLightLevel(new BigDecimal(lightLevel).setScale(2, RoundingMode.HALF_UP),
                    new LutronDuration(fadeTime), new LutronDuration(delayTime));
        } catch (IllegalArgumentException e) {
            logger.debug("Ignoring setLevel command due to illegal argument exception: {}", e.getMessage());
        }
    }

    /**
     * Static setLevel method for Rules DSL backward compatibility
     */
    public static void setLevel(ThingActions actions, @Nullable Double level, @Nullable Double fadeTime,
            @Nullable Double delayTime) {
        ((DimmerActions) actions).setLevel(level, fadeTime, delayTime);
    }
}
