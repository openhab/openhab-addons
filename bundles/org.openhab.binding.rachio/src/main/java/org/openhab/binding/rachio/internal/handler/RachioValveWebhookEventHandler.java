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
package org.openhab.binding.rachio.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatches Smart Hose Timer valve webhook events to Valve handlers.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
class RachioValveWebhookEventHandler implements RachioWebhookEventHandler {
    private final Logger logger = LoggerFactory.getLogger(RachioValveWebhookEventHandler.class);
    private final RachioBridgeHandler bridgeHandler;

    RachioValveWebhookEventHandler(RachioBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    public boolean supports(RachioWebhookResourceType resourceType) {
        return resourceType == RachioWebhookResourceType.VALVE;
    }

    @Override
    public boolean handle(RachioEventGsonDTO event) {
        boolean handled = false;
        for (RachioStatusListener listener : bridgeHandler.rachioStatusListeners) {
            if (listener instanceof RachioValveHandler valveHandler && valveHandler.handlesValveId(event.resourceId)) {
                handled |= valveHandler.webhookEvent(event);
            }
        }
        if (!handled) {
            logger.debug("RachioCloud: Valve event '{}' for unknown valve '{}'", event.eventType, event.resourceId);
        }
        return handled;
    }
}
