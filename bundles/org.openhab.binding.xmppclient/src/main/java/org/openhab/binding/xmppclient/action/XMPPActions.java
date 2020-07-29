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
package org.openhab.binding.xmppclient.action;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.xmppclient.handler.XMPPClientHandler;
import org.openhab.binding.xmppclient.internal.XMPPClient;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the automation engine action handler service for the publishXMPP action.
 * <p>
 * <b>Note:</b>The static method <b>invokeMethodOf</b> handles the case where
 * the test <i>actions instanceof XMPPActions</i> fails. This test can fail
 * due to an issue in openHAB core v2.5.0 where the {@link XMPPActions} class
 * can be loaded by a different classloader than the <i>actions</i> instance.
 *
 * @author Pavel Gololobov - Initial contribution
 */
@ThingActionsScope(name = "xmpp")
@NonNullByDefault
public class XMPPActions implements ThingActions, IXMPPActions {
    private static final Logger logger = LoggerFactory.getLogger(XMPPActions.class);
    private @Nullable XMPPClientHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (XMPPClientHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    @Override
    @RuleAction(label = "publishXMPP", description = "Publish to XMPP")
    public void publishXMPP(@ActionInput(name = "to", label = "To", description = "Send to") @Nullable String to,
            @ActionInput(name = "text", label = "Text", description = "Message text") @Nullable String text) {
        XMPPClientHandler clientHandler = handler;
        if (clientHandler == null) {
            logger.warn("XMPP ThingHandler is null");
            return;
        }

        XMPPClient connection = clientHandler.getXMPPClient();
        if (connection == null) {
            logger.warn("XMPP ThingHandler connection is null");
            return;
        }
        if ((to == null) || (text == null)) {
            logger.info("Skipping XMPP messaging to {} value {}", to, text);
            return;
        }
        connection.sendMessage(to, text);
    }

    public static void publishXMPP(@Nullable ThingActions actions, @Nullable String to, @Nullable String text) {
        invokeMethodOf(actions).publishXMPP(to, text);
    }

    private static IXMPPActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(XMPPActions.class.getName())) {
            if (actions instanceof IXMPPActions) {
                return (IXMPPActions) actions;
            } else {
                return (IXMPPActions) Proxy.newProxyInstance(IXMPPActions.class.getClassLoader(),
                        new Class[] { IXMPPActions.class }, (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException("Actions is not an instance of XMPPActions");
    }
}
