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
package org.openhab.binding.robonect.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Holds the mower Thing configuration.
 *
 * @author Marco Meyer - Initial contribution
 * @author Nico Brüttner - Detailed polling, push credentials, and web interface settings
 */
@NonNullByDefault
public class RobonectConfig {

    private String host = "";

    private @Nullable String user;

    private @Nullable String password;

    private @Nullable String pushUser;
    private @Nullable String pushPass;

    private int pollInterval;

    private int offlineTimeout;

    private @Nullable String timezone;

    private int pollIntervalExtended;

    private boolean useAjaxApi;

    private boolean useWebApi;

    /**
     * Returns the configured mower host.
     *
     * @return the host address
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the configured username.
     *
     * @return the username
     */
    public @Nullable String getUser() {
        return user;
    }

    /**
     * Returns the configured password.
     *
     * @return the password
     */
    public @Nullable String getPassword() {
        return password;
    }

    /**
     * Returns the username expected on incoming push notifications.
     *
     * @return the configured push username, or {@code null} if unset
     */
    public @Nullable String getPushUser() {
        return pushUser;
    }

    /**
     * Returns the password expected on incoming push notifications.
     *
     * @return the configured push password, or {@code null} if unset
     */
    public @Nullable String getPushPass() {
        return pushPass;
    }

    /**
     * Returns the normal polling interval.
     *
     * @return the interval in seconds
     */
    public int getPollInterval() {
        return pollInterval;
    }

    /**
     * Returns the configured offline timeout.
     *
     * @return the timeout in minutes
     */
    public int getOfflineTimeout() {
        return offlineTimeout;
    }

    /**
     * Returns the mower time zone.
     *
     * @return the time zone identifier
     */
    public @Nullable String getTimezone() {
        return timezone;
    }

    /**
     * Returns the detailed polling interval.
     *
     * @return the interval in seconds, or zero when disabled
     */
    public int getPollIntervalExtended() {
        return pollIntervalExtended;
    }

    /**
     * Reports whether localized Ajax UI scraping is enabled.
     *
     * @return {@code true} if Ajax UI scraping is enabled
     */
    public boolean isAjaxApiEnabled() {
        return useAjaxApi;
    }

    /**
     * Reports whether unofficial web-interface operations are enabled.
     *
     * @return {@code true} if unofficial operations are enabled
     */
    public boolean isUseWebApi() {
        return useWebApi;
    }
}
