/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.xmppclient.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.xmppclient.internal.XMPPClient;
import org.openhab.binding.xmppclient.internal.handler.XMPPClientHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the automation engine action handler service for the publishXMPP action.
 *
 * @author Pavel Gololobov - Initial contribution
 */
@ThingActionsScope(name = "xmppclient")
@NonNullByDefault
public class XMPPActions implements ThingActions {
    private static final Logger logger = LoggerFactory.getLogger(XMPPActions.class);
    private @Nullable XMPPClientHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (XMPPClientHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "publish a message", description = "Publish a message using XMPP.")
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

    @RuleAction(label = "publish an image by HTTP", description = "Publish an image by HTTP using XMPP.")
    public void publishXMPPImageByHTTP(
            @ActionInput(name = "to", label = "To", description = "Send to") @Nullable String to,
            @ActionInput(name = "filename", label = "Filename", description = "Image Filename") @Nullable String filename) {
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
        if ((to == null) || (filename == null)) {
            logger.warn("Skipping XMPP messaging to {} value {}", to, filename);
            return;
        }
        connection.sendImageByHTTP(to, filename);
    }

    public static void publishXMPP(ThingActions actions, @Nullable String to, @Nullable String text) {
        ((XMPPActions) actions).publishXMPP(to, text);
    }

    public static void publishXMPPImageByHTTP(ThingActions actions, @Nullable String to, @Nullable String filename) {
        ((XMPPActions) actions).publishXMPPImageByHTTP(to, filename);
    }
}
