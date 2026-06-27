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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Non-user-facing extension point for future product-family webhook handlers.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
class RachioPlaceholderWebhookEventHandler implements RachioWebhookEventHandler {
    private final Logger logger = LoggerFactory.getLogger(RachioPlaceholderWebhookEventHandler.class);
    private final String familyName;
    private final Set<RachioWebhookResourceType> resourceTypes;

    RachioPlaceholderWebhookEventHandler(String familyName, Set<RachioWebhookResourceType> resourceTypes) {
        this.familyName = familyName;
        this.resourceTypes = resourceTypes;
    }

    @Override
    public boolean supports(RachioWebhookResourceType resourceType) {
        return resourceTypes.contains(resourceType);
    }

    @Override
    public boolean handle(RachioEventGsonDTO event) {
        logger.debug("RachioCloud: {} webhook event type '{}' for resource '{}' is not user-facing yet", familyName,
                event.eventType, event.resourceId);
        return false;
    }
}
