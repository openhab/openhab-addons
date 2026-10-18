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

/**
 * Handles webhook events for a resource family.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
interface RachioWebhookEventHandler {
    /**
     * Checks whether this handler supports the supplied webhook resource type.
     *
     * @param resourceType the webhook resource type
     * @return {@code true} if this handler supports the resource type
     */
    boolean supports(RachioWebhookResourceType resourceType);

    /**
     * Handles a webhook event.
     *
     * @param event the normalized webhook event
     * @return {@code true} if the event was handled
     */
    boolean handle(RachioEventGsonDTO event);
}
