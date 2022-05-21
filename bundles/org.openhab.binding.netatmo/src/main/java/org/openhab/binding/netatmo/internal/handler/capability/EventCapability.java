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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.binding.netatmo.internal.servlet.WebhookServlet;

/**
 * {@link EventCapability} is the base class for handlers subject to receive event notifications.
 * This class registers to NetatmoServletService so it can be notified when an event arrives.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class EventCapability extends Capability {
    private Optional<WebhookServlet> webhook = Optional.empty();

    public EventCapability(CommonInterface handler) {
        super(handler);
    }

    @Override
    public void initialize() {
        ApiBridgeHandler accountHandler = handler.getAccountHandler();
        if (accountHandler != null) {
            webhook = accountHandler.getWebHookServlet();
            webhook.ifPresent(servlet -> servlet.registerDataListener(handler.getId(), this));
        }
    }

    @Override
    public void dispose() {
        webhook.ifPresent(servlet -> servlet.unregisterDataListener(handler.getId()));
    }
}
