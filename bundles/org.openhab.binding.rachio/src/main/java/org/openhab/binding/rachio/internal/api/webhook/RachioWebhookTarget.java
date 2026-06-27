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
package org.openhab.binding.rachio.internal.api.webhook;

import static org.openhab.binding.rachio.internal.RachioUtils.urlEncode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.api.json.RachioApiGsonDTO.RachioApiWebHookEntry;

/**
 * Describes one desired modern WebhookService registration target.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
public class RachioWebhookTarget {
    private final String resourceId;
    private final RachioWebhookResourceType resourceType;
    private final LinkedHashSet<String> eventTypes;

    public RachioWebhookTarget(String resourceId, RachioWebhookResourceType resourceType,
            Collection<String> eventTypes) {
        this.resourceId = resourceId;
        this.resourceType = resourceType;
        this.eventTypes = new LinkedHashSet<>(eventTypes);
    }

    public static RachioWebhookTarget irrigationController(String deviceId, Collection<String> eventTypes) {
        return new RachioWebhookTarget(deviceId, RachioWebhookResourceType.IRRIGATION_CONTROLLER, eventTypes);
    }

    public RachioWebhookTarget withEventTypes(Collection<String> eventTypes) {
        return new RachioWebhookTarget(resourceId, resourceType, eventTypes);
    }

    public String getResourceId() {
        return resourceId;
    }

    public RachioWebhookResourceType getResourceType() {
        return resourceType;
    }

    public Set<String> getEventTypes() {
        return Set.copyOf(eventTypes);
    }

    public ArrayList<String> getEventTypeList() {
        return new ArrayList<>(eventTypes);
    }

    public String buildListQuery() {
        return resourceType.getQueryParameter() + "=" + urlEncode(resourceId);
    }

    public Map<String, Object> buildCreatePayload(String callbackUrl, String externalId) {
        return Map.of("resourceId", Map.of(resourceType.getJsonFieldName(), resourceId), "externalId", externalId,
                "url", callbackUrl, "eventTypes", getEventTypeList());
    }

    public boolean matches(RachioApiWebHookEntry webhook, String callbackUrl, String externalId) {
        return Objects.equals(webhook.url, callbackUrl) && Objects.equals(webhook.externalId, externalId)
                && resourceMatches(webhook) && eventTypesMatch(webhook.eventTypes);
    }

    public boolean resourceMatches(RachioApiWebHookEntry webhook) {
        return webhook.resourceId != null && Objects.equals(webhook.resourceId.getResourceId(resourceType), resourceId);
    }

    public boolean eventTypesMatch(@Nullable Collection<String> actualEventTypes) {
        if (actualEventTypes == null || actualEventTypes.isEmpty()) {
            return true;
        }
        return actualEventTypes.size() == eventTypes.size() && actualEventTypes.containsAll(eventTypes)
                && eventTypes.containsAll(actualEventTypes);
    }

    public Set<String> getUnsupportedEventTypes(Collection<String> supportedEventTypes) {
        LinkedHashSet<String> unsupportedEventTypes = new LinkedHashSet<>(eventTypes);
        unsupportedEventTypes.removeAll(supportedEventTypes);
        return unsupportedEventTypes;
    }

    public RachioWebhookTarget filterEventTypes(Collection<String> supportedEventTypes) {
        LinkedHashSet<String> filteredEventTypes = new LinkedHashSet<>(eventTypes);
        filteredEventTypes.retainAll(supportedEventTypes);
        return withEventTypes(filteredEventTypes);
    }

    public String describe() {
        return resourceType.getApiValue() + ":" + resourceId;
    }
}
