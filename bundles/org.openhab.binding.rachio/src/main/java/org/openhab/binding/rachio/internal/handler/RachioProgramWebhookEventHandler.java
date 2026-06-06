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
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO.RachioWebhookPayload;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatches Smart Hose Timer Program webhook events to matching Program Things.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
class RachioProgramWebhookEventHandler implements RachioWebhookEventHandler {
    private final Logger logger = LoggerFactory.getLogger(RachioProgramWebhookEventHandler.class);
    private final RachioBridgeHandler bridgeHandler;

    RachioProgramWebhookEventHandler(RachioBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    public boolean supports(RachioWebhookResourceType resourceType) {
        return resourceType == RachioWebhookResourceType.PROGRAM;
    }

    @Override
    public boolean handle(RachioEventGsonDTO event) {
        boolean handled = false;
        String programId = event.resourceId;
        RachioWebhookPayload payload = event.payload;
        if (programId.isBlank() && payload != null) {
            programId = payload.programId;
        }
        for (RachioStatusListener listener : bridgeHandler.rachioStatusListeners) {
            if (listener instanceof RachioValveProgramHandler programHandler
                    && programHandler.handlesProgramId(programId)) {
                handled |= programHandler.webhookEvent(event);
            }
        }
        if (!handled) {
            logger.debug("No Smart Hose Timer Program handler matched webhook event '{}' for program '{}'",
                    event.eventType, programId);
        }
        return handled;
    }
}
