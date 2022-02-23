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
package org.openhab.binding.netatmo.internal.handler.capability;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.SecurityApi;
import org.openhab.binding.netatmo.internal.handler.NetatmoHandler;
import org.openhab.binding.netatmo.internal.webhook.NetatmoServlet;
import org.openhab.core.thing.Bridge;

/**
 * {@link EventListenerCapability} is the base class for handlers
 * subject to receive event notifications. This class registers to webhookservlet so
 * it can be notified when an event arrives.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class EventListenerCapability extends Capability<SecurityApi> {
    private final NetatmoServlet servlet;

    public EventListenerCapability(Bridge bridge, ApiBridge apiBridge, NetatmoServlet webhookServlet) {
        super(bridge, apiBridge.getRestManager(SecurityApi.class));
        this.servlet = webhookServlet;
        NetatmoHandler handler = getNAHandler();
        if (handler != null) {
            servlet.registerDataListener(handler.getId(), this);
        }
    }

    public void dispose() {
        servlet.unregisterDataListener(this);
    }
}
