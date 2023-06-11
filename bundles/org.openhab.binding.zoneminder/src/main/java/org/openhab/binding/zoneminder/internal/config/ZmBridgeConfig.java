/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.zoneminder.internal.config;

import static org.openhab.binding.zoneminder.internal.ZmBindingConstants.DEFAULT_URL_PATH;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ZmBridgeConfig} class contains fields mapping thing configuration parameters.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class ZmBridgeConfig {

    /**
     * Host name or IP address of Zoneminder server
     */
    public String host = "";

    /**
     * Use http or https
     */
    public Boolean useSSL = Boolean.FALSE;

    /**
     * Port number
     */
    public @Nullable Integer portNumber;

    /**
     * URL fragment (e.g. /zm)
     */
    public String urlPath = DEFAULT_URL_PATH;

    /**
     * Frequency at which monitor status will be updated
     */
    public @Nullable Integer refreshInterval;

    /**
     * Alarm duration set on monitor things when they're discovered
     */
    public @Nullable Integer defaultAlarmDuration;

    /**
     * Default image refresh interval set on monitor things when they're discovered
     */
    public @Nullable Integer defaultImageRefreshInterval;

    /**
     * Enable/disable monitor discovery
     */
    public Boolean discoveryEnabled = Boolean.TRUE;

    /**
     * Zoneminder user name
     */
    public @Nullable String user;

    /**
     * Zoneminder password
     */
    public @Nullable String pass;
}
