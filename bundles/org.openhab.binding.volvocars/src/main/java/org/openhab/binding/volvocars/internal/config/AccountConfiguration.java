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
package org.openhab.binding.volvocars.internal.config;

import static org.openhab.binding.volvocars.internal.VolvoCarsBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration of the Volvo Cars account bridge.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class AccountConfiguration {
    public String clientId = "";
    public String clientSecret = "";
    public String apiKey = "";
    public String redirectUri = "";
    public String scopes = "";

    public boolean isComplete() {
        return !clientId.isBlank() && !clientSecret.isBlank() && !apiKey.isBlank() && !redirectUri.isBlank();
    }

    /**
     * @return true if the application was registered with the legacy Volvo ID service
     */
    public boolean isLegacyClient() {
        return clientId.startsWith(LEGACY_CLIENT_ID_PREFIX);
    }

    public String authorizeUrl() {
        return isLegacyClient() ? LEGACY_AUTHORIZE_URL : AUTHORIZE_URL;
    }

    public String tokenUrl() {
        return isLegacyClient() ? LEGACY_TOKEN_URL : TOKEN_URL;
    }
}
