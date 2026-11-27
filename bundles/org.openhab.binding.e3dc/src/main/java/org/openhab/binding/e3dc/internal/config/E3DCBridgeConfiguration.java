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
package org.openhab.binding.e3dc.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The configuration of the bridge.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCBridgeConfiguration {

    public static final int MIN_REFRESH_INTERVAL_SECONDS = 1;

    public static final int MAX_REFRESH_INTERVAL_SECONDS = 300;

    public static final int MIN_HISTORY_INTERVAL_MINUTES = 1;

    public static final int MAX_HISTORY_INTERVAL_MINUTES = 1440;

    public static final int MIN_TIMEOUT_SECONDS = 1;

    /** Chosen so the conversion to milliseconds stays in the integer range. */
    public static final int MAX_TIMEOUT_SECONDS = 60;

    public String host = "";

    public int port = 5033;

    public String userName = "";

    public String password = "";

    /** The encryption key of the connection, which is not the local user password. */
    public String rscpPassword = "";

    public int refreshInterval = 5;

    public int historyInterval = 15;

    public int timeout = 10;

    /** The poll interval limited to the supported range; things file values are not checked against the metadata. */
    /**
     * The poll interval in seconds, at least one second.
     */
    public int refreshIntervalSeconds() {
        return Math.clamp(refreshInterval, MIN_REFRESH_INTERVAL_SECONDS, MAX_REFRESH_INTERVAL_SECONDS);
    }

    /**
     * The interval in seconds between two histories.
     */
    public int historyIntervalSeconds() {
        int minutes = Math.clamp(historyInterval, MIN_HISTORY_INTERVAL_MINUTES, MAX_HISTORY_INTERVAL_MINUTES);
        return Math.max(minutes * 60, refreshIntervalSeconds());
    }

    /**
     * The timeout of one request in seconds.
     */
    public int timeoutSeconds() {
        return Math.clamp(timeout, MIN_TIMEOUT_SECONDS, MAX_TIMEOUT_SECONDS);
    }
}
