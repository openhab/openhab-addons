/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.ntp.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link NtpThingConfiguration} is responsible for holding
 * the thing configuration settings
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class NtpThingConfiguration {

    public static final String HOSTNAME = "hostname";
    public static final String REFRESH_INTERVAL = "refreshInterval";
    public static final String REFRESH_NTP = "refreshNtp";
    public static final String SERVER_PORT = "serverPort";
    public static final String TIMEZONE = "timeZone";

    private static final String DEFAULT_SERVER_HOSTNAME = "0.pool.ntp.org";
    private static final int DEFAULT_REFRESH_INTERVAL = 60;
    private static final int DEFAULT_REFRESH_NTP = 30;
    private static final int DEFAULT_SERVER_PORT = 123;

    public @Nullable String hostname;
    public @Nullable Integer refreshInterval;
    public @Nullable Integer refreshNtp;
    public @Nullable Integer serverPort;
    public @Nullable String timeZone;

    public String getHostname() {
        String name = hostname;
        return name != null && !name.trim().isEmpty() ? name.trim() : DEFAULT_SERVER_HOSTNAME;
    }

    public int getRefreshInterval() {
        Integer interval = refreshInterval;
        return interval != null ? interval : DEFAULT_REFRESH_INTERVAL;
    }

    public int getRefreshNtp() {
        Integer number = refreshNtp;
        return number != null ? number : DEFAULT_REFRESH_NTP;
    }

    public int getServerPort() {
        Integer port = serverPort;
        return port != null ? port : DEFAULT_SERVER_PORT;
    }

    public @Nullable String getTimeZone() {
        return timeZone;
    }
}
