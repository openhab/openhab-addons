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
 * {@link WebhookListenerCapability} is the class used to register for webhook device_event(s)
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public abstract class WebhookListenerCapability extends Capability {
    private @Nullable WebhookServlet servlet;

    public WebhookListenerCapability(CommonInterface handler) {
        super(handler);
    }

    @Override
    // We'll switch to push mode if possible and only after a first reading with pull mode
    protected void afterNewData(@Nullable Object newData) {
        if (handler.getAccountHandler() instanceof ApiBridgeHandler accountHandler
                && accountHandler.getWebhookServlet() instanceof WebhookServlet whs && !whs.equals(servlet)) {
            if (moduleType.getMonitored().isEmpty()) {
                whs.registerDataListener(handler.getId(), this);
            } else {
                whs.monitorEvents(handler.getId(), this, moduleType.getMonitored());
            }
            servlet = whs;
        }
        super.afterNewData(newData);
    }

    public boolean pullMode() {
        return servlet == null;
    }

    @Override
    public void dispose() {
        if (servlet instanceof WebhookServlet whs) {
            whs.unregisterDataListener(handler.getId());
            servlet = null;
        }
        super.dispose();
    }
}
