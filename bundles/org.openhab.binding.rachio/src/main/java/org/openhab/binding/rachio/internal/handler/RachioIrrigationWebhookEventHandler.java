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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatches Smart Irrigation Controller webhook events to controller, zone, and schedule handlers.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
class RachioIrrigationWebhookEventHandler implements RachioWebhookEventHandler {
    private final Logger logger = LoggerFactory.getLogger(RachioIrrigationWebhookEventHandler.class);
    private final RachioBridgeHandler bridgeHandler;

    RachioIrrigationWebhookEventHandler(RachioBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    public boolean supports(RachioWebhookResourceType resourceType) {
        return resourceType == RachioWebhookResourceType.IRRIGATION_CONTROLLER;
    }

    @Override
    public boolean handle(RachioEventGsonDTO event) {
        try {
            RachioDevice controller = bridgeHandler.findIrrigationController(event.deviceId);
            if (controller == null) {
                logger.debug("RachioCloud: Event {}.{} for unknown irrigation controller {}", event.type, event.subType,
                        event.deviceId);
                return false;
            }

            boolean handled = false;
            RachioDeviceHandler deviceHandler = resolveDeviceHandler(controller, event.deviceId);
            if (deviceHandler != null) {
                handled |= deviceHandler.webhookEvent(event);
            }
            for (RachioStatusListener listener : bridgeHandler.rachioStatusListeners) {
                if (listener instanceof RachioScheduleHandler scheduleHandler) {
                    handled |= scheduleHandler.webhookEvent(event);
                }
            }
            if (handled) {
                return true;
            }
            logger.debug(
                    "RachioCloud: Event {}.{} matched irrigation controller {} but was not directly handled; reconciliation remains available",
                    event.type, event.subType, event.deviceId);
        } catch (RuntimeException e) {
            logger.debug("RachioCloud: Unable to process irrigation event {}.{} for device {}", event.category,
                    event.type, event.deviceId, e);
        }
        return false;
    }

    private @Nullable RachioDeviceHandler resolveDeviceHandler(RachioDevice controller, String controllerId) {
        RachioDeviceHandler deviceHandler = controller.getThingHandler();
        if (deviceHandler != null) {
            return deviceHandler;
        }
        for (RachioStatusListener listener : bridgeHandler.rachioStatusListeners) {
            if (listener instanceof RachioDeviceHandler candidate && candidate.handlesController(controllerId)) {
                candidate.rebindToCurrentModel(controller, "webhook dispatch");
                return candidate;
            }
        }
        return null;
    }
}
