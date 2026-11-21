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
 * The configuration for {@link org.openhab.binding.ojelectronics.internal.OJCloudHandler}
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class OJElectronicsBridgeConfiguration {

    /**
     * Password
     */
    public String password = "";

    /**
     * Customer-ID
     */
    private int customerId = 1;

    /**
     * User Name
     */
    public String userName = "";

    /**
     * Url for API
     */
    private String apiUrl = "https://OWD5-OJ001-App.ojelectronics.com";

    /**
     * API-Key
     */
    public String apiKey = "";

    /**
     * Software Version
     */
    public int softwareVersion = 1060;

    /**
     * Cloud vendor preset. Defaults to {@link CloudVendor#CUSTOM} (pass-through to the fields
     * above) rather than {@link CloudVendor#OJELECTRONICS}, so that a bridge configured before
     * this parameter existed - which therefore has no stored value for it - keeps using its own
     * {@link #apiUrl}/{@link #customerId} unchanged. The config description default shown for
     * newly created bridges is {@link CloudVendor#OJELECTRONICS} instead, which is purely a UI
     * convenience and behaviorally equivalent for a fresh bridge.
     */
    public String cloudVendor = CloudVendor.CUSTOM.name();

    private @Nullable String restApiUrl;

    /*
     * Gets the Api-URL
     */
    public String getRestApiUrl() {
        String localRestApiUrl = restApiUrl;
        if (localRestApiUrl == null) {
            String resolvedApiUrl = CloudVendor.valueOf(cloudVendor).resolveApiUrl(apiUrl);
            localRestApiUrl = restApiUrl = resolvedApiUrl.replace("/api", "") + "/api";
        }
        return localRestApiUrl;
    }

    private @Nullable String signalRApiUrl;

    /*
     * Gets the SignalR Notification URL
     */
    public String getSignalRUrl() {
        String localSignalRApiUrl = signalRApiUrl;
        if (localSignalRApiUrl == null) {
            String resolvedApiUrl = CloudVendor.valueOf(cloudVendor).resolveApiUrl(apiUrl);
            localSignalRApiUrl = signalRApiUrl = resolvedApiUrl.replace("/api", "") + "/ocd5notification";
        }
        return localSignalRApiUrl;
    }

    /*
     * Gets the Customer-ID
     */
    public int getCustomerId() {
        return CloudVendor.valueOf(cloudVendor).resolveCustomerId(customerId);
    }
}
