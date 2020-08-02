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
package org.openhab.binding.dmx.action;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.dmx.internal.DmxBridgeHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DmxActions} provides actions for DMX Bridges
 * <p>
 * <b>Note:</b>The static method <b>invokeMethodOf</b> handles the case where
 * the test <i>actions instanceof DmxActions</i> fails. This test can fail
 * due to an issue in openHAB core v2.5.0 where the {@link DmxActions} class
 * can be loaded by a different classloader than the <i>actions</i> instance.
 *
 * @author Jan N. Klug - Initial contribution
 */

@ThingActionsScope(name = "dmx")
@NonNullByDefault
public class DmxActions implements ThingActions, IDmxActions {

    private final Logger logger = LoggerFactory.getLogger(DmxActions.class);

    private @Nullable DmxBridgeHandler handler;

    @Override
    @RuleAction(label = "DMX Output", description = "immediately performs fade on selected DMX channels")
    public void sendFade(@ActionInput(name = "channels") @Nullable String channels,
            @ActionInput(name = "fade") @Nullable String fade,
            @ActionInput(name = "resumeAfter") @Nullable Boolean resumeAfter) {
        logger.debug("thingHandlerAction called with inputs: {} {} {}", channels, fade, resumeAfter);

        if (handler == null) {
            logger.warn("DMX Action service ThingHandler is null!");
            return;
        }

        if (channels == null) {
            logger.debug("skipping immediate DMX action {} due to missing channel(s)", fade);
            return;
        }

        if (fade == null) {
            logger.debug("skipping immediate DMX action channel(s) {} due to missing fade", channels);
            return;
        }

        if (resumeAfter == null) {
            logger.debug("DMX action {} to channel(s) {} with default resumeAfter=false", fade, channels);
            handler.immediateFade(channels, fade, false);
        } else {
            handler.immediateFade(channels, fade, resumeAfter);
        }
    }

    public static void sendFade(@Nullable ThingActions actions, @Nullable String channels, @Nullable String fade,
            @Nullable Boolean resumeAfter) {
        invokeMethodOf(actions).sendFade(channels, fade, resumeAfter);
    }

    private static IDmxActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(DmxActions.class.getName())) {
            if (actions instanceof IDmxActions) {
                return (IDmxActions) actions;
            } else {
                return (IDmxActions) Proxy.newProxyInstance(IDmxActions.class.getClassLoader(),
                        new Class[] { IDmxActions.class }, (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException("Actions is not an instance of DmxActions");
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof DmxBridgeHandler) {
            this.handler = (DmxBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }
}
