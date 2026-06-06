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

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Routes validated WebhookService events to resource-family handlers.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
public class RachioWebhookDispatcher {
    private final Logger logger = LoggerFactory.getLogger(RachioWebhookDispatcher.class);
    private final List<RachioWebhookEventHandler> handlers;

    RachioWebhookDispatcher(List<RachioWebhookEventHandler> handlers) {
        this.handlers = handlers;
    }

    public static RachioWebhookDispatcher createDefault(RachioBridgeHandler bridgeHandler) {
        return new RachioWebhookDispatcher(List.of(new RachioIrrigationWebhookEventHandler(bridgeHandler),
                new RachioValveWebhookEventHandler(bridgeHandler), new RachioProgramWebhookEventHandler(bridgeHandler),
                new RachioPlaceholderWebhookEventHandler("Smart Lighting",
                        Set.of(RachioWebhookResourceType.LIGHTING_CONTROLLER, RachioWebhookResourceType.LIGHTING_ZONE,
                                RachioWebhookResourceType.LIGHTING_SCENE,
                                RachioWebhookResourceType.LIGHTING_PROGRAM))));
    }

    public boolean dispatch(RachioEventGsonDTO event) {
        RachioWebhookResourceType resourceType = resolveResourceType(event);
        logger.debug("RachioCloud: Dispatch webhook eventType='{}', resourceType='{}', resourceId='{}'",
                event.eventType, resourceType.getApiValue(), event.resourceId);
        for (RachioWebhookEventHandler handler : handlers) {
            if (handler.supports(resourceType)) {
                return handler.handle(event);
            }
        }
        logger.debug("RachioCloud: No webhook handler registered for resourceType='{}', eventType='{}'",
                event.resourceType, event.eventType);
        return false;
    }

    private RachioWebhookResourceType resolveResourceType(RachioEventGsonDTO event) {
        RachioWebhookResourceType resourceType = RachioWebhookResourceType.fromApiValue(event.resourceType);
        if (resourceType.isKnown()) {
            return resourceType;
        }
        if (!event.deviceId.isBlank() || "DEVICE".equals(event.category) || "ZONE".equals(event.category)
                || "SCHEDULE".equals(event.category)) {
            return RachioWebhookResourceType.IRRIGATION_CONTROLLER;
        }
        return RachioWebhookResourceType.UNKNOWN;
    }
}
