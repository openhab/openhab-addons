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
package org.openhab.binding.ojelectronics.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Presets for the different clouds operated by (or for) OJ Electronics.
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public enum CloudVendor {
    OJELECTRONICS("https://OWD5-OJ001-App.ojelectronics.com", 1),
    SWATT("https://OWD5-OJ001-App.ojelectronics.com", 99),
    SCHLUETER("https://owd5-sl003-app.ojelectronics.com", 3),
    MICROTEMP("https://owd5-mh015-app.ojelectronics.com", 111),
    CUSTOM(null, null);

    private final @Nullable String apiUrl;
    private final @Nullable Integer customerId;

    CloudVendor(@Nullable String apiUrl, @Nullable Integer customerId) {
        this.apiUrl = apiUrl;
        this.customerId = customerId;
    }

    public String resolveApiUrl(String configuredApiUrl) {
        String preset = apiUrl;
        return preset != null ? preset : configuredApiUrl;
    }

    public int resolveCustomerId(int configuredCustomerId) {
        Integer preset = customerId;
        return preset != null ? preset : configuredCustomerId;
    }
}
