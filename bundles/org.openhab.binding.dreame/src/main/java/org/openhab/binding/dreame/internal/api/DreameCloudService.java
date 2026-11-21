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
package org.openhab.binding.dreame.internal.api;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Brand-specific cloud settings shared by Dreamehome and MOVAhome.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public enum DreameCloudService {
    DREAMEHOME("dreamehome", "Dreamehome", ".iot.dreame.tech:13267", "000000",
            "Dreame_Smarthome/1.5.59 (iPhone; iOS 16.0; Scale/3.00)"),
    MOVAHOME("movahome", "MOVAhome", ".iot.mova-tech.com:13267", "000002",
            "Mova_Smarthome/1.5.59 (iPhone; iOS 16.0; Scale/3.00)");

    private static final String AUTHORIZATION = "Basic ZHJlYW1lX2FwcHYxOkFQXmR2QHpAU1FZVnhOODg=";

    private final String configurationValue;
    private final String label;
    private final String apiSuffix;
    private final String tenantId;
    private final String userAgent;

    DreameCloudService(String configurationValue, String label, String apiSuffix, String tenantId, String userAgent) {
        this.configurationValue = configurationValue;
        this.label = label;
        this.apiSuffix = apiSuffix;
        this.tenantId = tenantId;
        this.userAgent = userAgent;
    }

    public String configurationValue() {
        return configurationValue;
    }

    public String label() {
        return label;
    }

    String apiUrl(String region) {
        return "https://" + region + apiSuffix;
    }

    String tenantId() {
        return tenantId;
    }

    String authorization() {
        return AUTHORIZATION;
    }

    String userAgent() {
        return userAgent;
    }

    public static DreameCloudService fromConfiguration(String value) throws DreameCloudException {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (DreameCloudService service : values()) {
            if (service.configurationValue.equals(normalized)) {
                return service;
            }
        }
        throw new DreameCloudException("Unsupported cloud service: " + value);
    }
}
