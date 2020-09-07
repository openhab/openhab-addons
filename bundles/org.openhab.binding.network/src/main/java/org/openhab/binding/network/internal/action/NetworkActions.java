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
package org.openhab.binding.network.internal.action;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.network.internal.handler.NetworkHandler;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class is responsible to call corresponding actions on {@link NetworkHandler}.
 * <p>
 * <b>Note:</b>The static method <b>invokeMethodOf</b> handles the case where
 * the test <i>actions instanceof NetworkActions</i> fails. This test can fail
 * due to an issue in openHAB core v2.5.0 where the {@link NetworkActions} class
 * can be loaded by a different classloader than the <i>actions</i> instance.
 *
 * @author Wouter Born - Initial contribution
 */
@ThingActionsScope(name = "network")
@NonNullByDefault
public class NetworkActions implements ThingActions, INetworkActions {

    private final Logger logger = LoggerFactory.getLogger(NetworkActions.class);

    private @Nullable NetworkHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof NetworkHandler) {
            this.handler = (NetworkHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @Override
    @RuleAction(label = "Send WoL Packet", description = "Send a Wake-on-LAN packet to wake the device")
    public void sendWakeOnLanPacket() {
        NetworkHandler localHandler = handler;
        if (localHandler != null) {
            localHandler.sendWakeOnLanPacket();
        } else {
            logger.warn("Failed to send Wake-on-LAN packet (handler null)");
        }
    }

    public static void sendWakeOnLanPacket(@Nullable ThingActions actions) {
        invokeMethodOf(actions).sendWakeOnLanPacket();
    }

    private static INetworkActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(NetworkActions.class.getName())) {
            if (actions instanceof INetworkActions) {
                return (INetworkActions) actions;
            } else {
                return (INetworkActions) Proxy.newProxyInstance(INetworkActions.class.getClassLoader(),
                        new Class[] { INetworkActions.class }, (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException("Actions is not an instance of " + NetworkActions.class.getName());
    }
}
