/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.lutron.action;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.lutron.internal.handler.DimmerHandler;
import org.openhab.binding.lutron.internal.protocol.LutronDuration;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DimmerActions} defines thing actions for DimmerHandler.
 *
 * @author Bob Adair - Initial contribution
 */
@ThingActionsScope(name = "lutron")
@NonNullByDefault
public class DimmerActions implements ThingActions, IDimmerActions {
    private final Logger logger = LoggerFactory.getLogger(DimmerActions.class);

    private @Nullable DimmerHandler handler;

    public DimmerActions() {
        logger.trace("Lutron Dimmer actions service created");
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof DimmerHandler) {
            this.handler = (DimmerHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    /**
     * The setLevel dimmer thing action
     */
    @Override
    @RuleAction(label = "setLevel", description = "Send set level command with fade and delay times")
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
            dimmerHandler.setLightLevel(new BigDecimal(lightLevel).setScale(2, BigDecimal.ROUND_HALF_UP),
                    new LutronDuration(fadeTime), new LutronDuration(delayTime));
        } catch (IllegalArgumentException e) {
            logger.debug("Ignoring setLevel command due to illegal argument exception: {}", e.getMessage());
        }
    }

    /**
     * Static setLevel method for Rules DSL backward compatibility
     */
    public static void setLevel(@Nullable ThingActions actions, @Nullable Double level, @Nullable Double fadeTime,
            @Nullable Double delayTime) {
        invokeMethodOf(actions).setLevel(level, fadeTime, delayTime); // Replace when core issue #1536 is fixed
    }

    /**
     * This is only necessary to work around a bug in openhab-core (issue #1536). It should be removed once that is
     * resolved.
     */
    private static IDimmerActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(DimmerActions.class.getName())) {
            if (actions instanceof IDimmerActions) {
                return (IDimmerActions) actions;
            } else {
                return (IDimmerActions) Proxy.newProxyInstance(IDimmerActions.class.getClassLoader(),
                        new Class[] { IDimmerActions.class }, (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException("Actions is not an instance of DimmerActions");
    }
}
