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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
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

    @Override
    @RuleAction(label = "setLightLevel", description = "Send light level command with fade and delay times")
    public void setLightLevel(
            @ActionInput(name = "level", label = "level", description = "Level") @Nullable DecimalType level,
            @ActionInput(name = "fadeTime", label = "fadeTime", description = "Fade time") @Nullable DecimalType fadeTime,
            @ActionInput(name = "delayTime", label = "delayTime", description = "Delay time") @Nullable DecimalType delayTime) {
        DimmerHandler dimmerHandler = handler;
        if (dimmerHandler == null) {
            logger.warn("Handler not set for Dimmer thing actions.");
            return;
        }

        // TODO - Allow null values for fade & delay
        if (level == null) {
            logger.debug("Ignoring setLightLevel command due to null value.");
            return;
        }
        if (fadeTime == null) {
            logger.debug("Ignoring setLightLevel command '{}' due to null value for fade time.", level);
            return;
        }
        if (delayTime == null) {
            logger.debug("Ignoring setLightLevel command '{}' due to null value for delay time.", level);
            return;
        }

        dimmerHandler.setLightLevel(level, new LutronDuration(fadeTime.toBigDecimal()),
                new LutronDuration(delayTime.toBigDecimal()));
    }

    // Static method for Rules DSL backward compatibility
    public static void setLightLevel(@Nullable ThingActions actions, @Nullable DecimalType level,
            @Nullable DecimalType fadeTime, @Nullable DecimalType delayTime) {
        invokeMethodOf(actions).setLightLevel(level, fadeTime, delayTime); // Remove when core issue #1536 is fixed
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
