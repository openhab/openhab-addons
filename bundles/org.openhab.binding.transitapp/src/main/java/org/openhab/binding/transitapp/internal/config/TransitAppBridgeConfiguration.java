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
package org.openhab.binding.transitapp.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class TransitAppBridgeConfiguration {
    public String apiKey = "";

    // API Performance & Caching Parameters
    public long cacheTimeMs = 30_000; // Cache API responses for 30 seconds
    public int retryAfterSeconds = 60; // Default retry-after value on rate limiting
    public int maxDepartures = 10; // Maximum number of departures to fetch per stop
}
