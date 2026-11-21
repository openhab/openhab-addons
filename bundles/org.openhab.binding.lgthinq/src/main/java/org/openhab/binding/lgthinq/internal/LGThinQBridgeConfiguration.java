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
package org.openhab.binding.lgthinq.internal;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link LGThinQBridgeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinQBridgeConfiguration {
    /**
     * Sample configuration parameters. Replace with your own.
     */
    public String username = "";
    public String password = "";
    public String country = "";
    public String language = "";
    public String manualCountry = "";
    public String manualLanguage = "";
    public int pollingIntervalSec = 0;
    public String alternativeServer = "";

    public LGThinQBridgeConfiguration() {
    }

    public LGThinQBridgeConfiguration(String username, String password, String country, String language,
            int pollingIntervalSec, String alternativeServer) {
        this.username = username;
        this.password = password;
        this.country = country;
        this.language = language;
        this.pollingIntervalSec = pollingIntervalSec;
        this.alternativeServer = alternativeServer;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getCountry() {
        String selectedCountry = "--".equals(country) ? manualCountry.toUpperCase(Locale.ROOT) : country;
        // LG's API expects an ISO 3166-1 alpha-2 country code. "UK" is a common mistake for the United Kingdom,
        // whose correct code is "GB". A mismatched country is accepted during gateway discovery and pre-login but
        // rejected with an HTTP 412 (Precondition Failed) at the EMP session login endpoint, so normalize it here.
        if ("UK".equalsIgnoreCase(selectedCountry)) {
            return "GB";
        }
        return selectedCountry;
    }

    public String getLanguage() {
        if ("--".equals(language)) {
            return manualLanguage;
        }
        return language;
    }

    public int getPollingIntervalSec() {
        return pollingIntervalSec;
    }

    public String getAlternativeServer() {
        return alternativeServer;
    }
}
