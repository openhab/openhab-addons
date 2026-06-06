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

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Resource categories supported by the modern Rachio WebhookService.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
public enum RachioWebhookResourceType {
    IRRIGATION_CONTROLLER("IRRIGATION_CONTROLLER", WEBHOOK_QUERY_CONTROLLER_ID, "irrigationControllerId"),
    VALVE("VALVE", WEBHOOK_QUERY_VALVE_ID, "valveId"),
    PROGRAM("PROGRAM", WEBHOOK_QUERY_PROGRAM_ID, "programId"),
    LIGHTING_CONTROLLER("LIGHTING_CONTROLLER", WEBHOOK_QUERY_LIGHTING_CONTROLLER_ID, "lightingControllerId"),
    LIGHTING_ZONE("LIGHTING_ZONE", WEBHOOK_QUERY_LIGHTING_ZONE_ID, "lightingZoneId"),
    LIGHTING_SCENE("LIGHTING_SCENE", WEBHOOK_QUERY_LIGHTING_SCENE_ID, "lightingSceneId"),
    LIGHTING_PROGRAM("LIGHTING_PROGRAM", WEBHOOK_QUERY_LIGHTING_PROGRAM_ID, "lightingProgramId"),
    UNKNOWN("", "", "");

    private final String apiValue;
    private final String queryParameter;
    private final String jsonFieldName;

    private RachioWebhookResourceType(String apiValue, String queryParameter, String jsonFieldName) {
        this.apiValue = apiValue;
        this.queryParameter = queryParameter;
        this.jsonFieldName = jsonFieldName;
    }

    public String getApiValue() {
        return apiValue;
    }

    public String getQueryParameter() {
        return queryParameter;
    }

    public String getJsonFieldName() {
        return jsonFieldName;
    }

    public boolean isKnown() {
        return this != UNKNOWN;
    }

    public static RachioWebhookResourceType fromApiValue(String value) {
        if (value.isBlank()) {
            return UNKNOWN;
        }
        String normalizedValue = value.trim().replace('-', '_').toUpperCase(Locale.ROOT);
        for (RachioWebhookResourceType type : values()) {
            if (type != UNKNOWN && type.apiValue.equals(normalizedValue)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
