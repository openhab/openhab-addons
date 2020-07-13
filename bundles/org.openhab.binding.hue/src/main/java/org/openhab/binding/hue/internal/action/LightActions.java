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
package org.openhab.binding.hue.internal.action;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.hue.internal.handler.HueLightHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LightActions} defines the thing actions for the hue binding.
 * <p>
 * <b>Note:</b>The static method <b>invokeMethodOf</b> handles the case where
 * the test <i>actions instanceof LightActions</i> fails. This test can fail
 * due to an issue in openHAB core v2.5.0 where the {@link LightActions} class
 * can be loaded by a different classloader than the <i>actions</i> instance.
 *
 * @author Jochen Leopold - Initial contribution
 * @author Laurent Garnier - new method invokeMethodOf + interface ILightActions
 */
@ThingActionsScope(name = "hue")
@NonNullByDefault
public class LightActions implements ThingActions, ILightActions {
    private final Logger logger = LoggerFactory.getLogger(LightActions.class);
    private @Nullable HueLightHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (HueLightHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    @Override
    @RuleAction(label = "@text/actionLabel", description = "@text/actionDesc")
    public void fadingLightCommand(
            @ActionInput(name = "channel", label = "@text/actionInputChannelLabel", description = "@text/actionInputChannelDesc") @Nullable String channel,
            @ActionInput(name = "command", label = "@text/actionInputCommandLabel", description = "@text/actionInputCommandDesc") @Nullable Command command,
            @ActionInput(name = "fadeTime", label = "@text/actionInputFadeTimeLabel", description = "@text/actionInputFadeTimeDesc") @Nullable DecimalType fadeTime) {
        HueLightHandler lightHandler = handler;
        if (lightHandler == null) {
            logger.warn("Hue Action service ThingHandler is null!");
            return;
        }

        if (channel == null) {
            logger.debug("skipping Hue fadingLightCommand to channel '{}' due to null value.", channel);
            return;
        }

        if (command == null) {
            logger.debug("skipping Hue fadingLightCommand to command '{}' due to null value.", command);
            return;
        }
        if (fadeTime == null) {
            logger.debug("skipping Hue fadingLightCommand to fadeTime '{}' due to null value.", fadeTime);
            return;
        }

        lightHandler.handleCommand(channel, command, fadeTime.longValue());
        logger.debug("send LightAction to {} with {}ms of fadeTime", channel, fadeTime);
    }

    private static ILightActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(LightActions.class.getName())) {
            if (actions instanceof ILightActions) {
                return (ILightActions) actions;
            } else {
                return (ILightActions) Proxy.newProxyInstance(ILightActions.class.getClassLoader(),
                        new Class[] { ILightActions.class }, (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException("Actions is not an instance of LightActions");
    }

    public static void fadingLightCommand(@Nullable ThingActions actions, @Nullable String channel,
            @Nullable Command command, @Nullable DecimalType fadeTime) {
        invokeMethodOf(actions).fadingLightCommand(channel, command, fadeTime);
    }
}
