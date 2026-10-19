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
package org.openhab.binding.e3dc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration of the bridge thing, which represents the connection to one E3/DC device.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCBridgeConfiguration {

    /** Shortest poll interval the binding accepts; a shorter one would query the device without pause. */
    public static final int MIN_REFRESH_INTERVAL_SECONDS = 1;

    /** Longest poll interval the binding accepts, the range the RSCP tooling allows for its own cycle. */
    public static final int MAX_REFRESH_INTERVAL_SECONDS = 300;

    /** Shortest interval of the history request in minutes. */
    public static final int MIN_HISTORY_INTERVAL_MINUTES = 1;

    /** Longest interval of the history request in minutes, one day. */
    public static final int MAX_HISTORY_INTERVAL_MINUTES = 1440;

    /** Host name or IP address of the device. */

    public String host = "";

    /** RSCP port. */
    public int port = 5033;

    /** Local user name of the device. */
    public String userName = "";

    /** Local user password of the device. */
    public String password = "";

    /** RSCP password as configured on the device; the encryption key of the connection. */
    public String rscpPassword = "";

    /** Poll interval in seconds. */
    public int refreshInterval = 5;

    /** Interval of the history request in minutes, independent of the poll interval. */
    public int historyInterval = 15;

    /** Socket timeout in seconds. */
    public int timeout = 10;

    /**
     * Token of the E3/DC cloud, taken from the portal.
     *
     * <p>
     * The cloud has no login of its own, so the token the portal hands to the browser is the starting
     * point. The binding renews it from then on and keeps the successor in the openHAB user data.
     */
    public String cloudToken = "";

    /** The installation in the cloud that owns this power plant. */
    public String installationId = "";

    /** The storage in the cloud whose price based charging is controlled. */
    public String storageId = "";

    /**
     * The configured poll interval limited to the supported range.
     *
     * <p>
     * The bounds in the thing metadata only restrict what the user interface offers; a value from a
     * things file or the REST API is not checked against them. An interval of zero or less would make
     * the scheduler run the next cycle immediately, so the value is limited here as well.
     */
    public int refreshIntervalSeconds() {
        return Math.clamp(refreshInterval, MIN_REFRESH_INTERVAL_SECONDS, MAX_REFRESH_INTERVAL_SECONDS);
    }

    /**
     * The configured history interval limited to the supported range, in seconds.
     *
     * <p>
     * The history counters change slowly, so they are requested less often than the live values. The
     * interval can not be shorter than the poll interval, because the history rides along with a poll.
     */
    public int historyIntervalSeconds() {
        int minutes = Math.clamp(historyInterval, MIN_HISTORY_INTERVAL_MINUTES, MAX_HISTORY_INTERVAL_MINUTES);
        return Math.max(minutes * 60, refreshIntervalSeconds());
    }
}
