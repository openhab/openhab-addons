/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler.capability;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.binding.netatmo.internal.servlet.WebhookServlet;

/**
 * {@link WebhookListenerCapability} is the class used to listen upon webhook device_event(s)
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class WebhookListenerCapability extends Capability {

    private @Nullable WebhookServlet webhookServlet;

    public WebhookListenerCapability(CommonInterface handler) {
        super(handler);
    }

    @Override
    public void initialize() {
        if (handler.getAccountHandler() instanceof ApiBridgeHandler accountHandler) {
            webhookServlet = null;
            accountHandler.getWebHookServlet().ifPresent(servlet -> {
                webhookServlet = servlet;
                servlet.registerDataListener(handler.getId(), this);
            });
        }
    }

    public boolean pullMode() {
        return webhookServlet == null;
    }

    @Override
    public void dispose() {
        if (webhookServlet instanceof WebhookServlet webhook) {
            webhook.unregisterDataListener(handler.getId());
            webhookServlet = null;
        }
        super.dispose();
    }
}
