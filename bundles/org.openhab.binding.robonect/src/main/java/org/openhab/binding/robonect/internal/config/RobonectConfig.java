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

/**
 * Holds the mower Thing configuration.
 *
 * @author Marco Meyer - Initial contribution
 */
public class RobonectConfig {

    private String host;

    private String user;

    private String password;

    private int pollInterval;

    private int offlineTimeout;

    private String timezone;

    private int detailedPollInterval;

    private boolean ajaxUiScraping;

    private boolean useUnofficialApi;

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
    public String getUser() {
        return user;
    }

    /**
     * Returns the configured password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
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
    public String getTimezone() {
        return timezone;
    }

    /**
     * Returns the detailed polling interval.
     *
     * @return the interval in seconds, or zero when disabled
     */
    public int getDetailedPollInterval() {
        return detailedPollInterval;
    }

    /**
     * Reports whether localized Ajax UI scraping is enabled.
     *
     * @return {@code true} if Ajax UI scraping is enabled
     */
    public boolean isAjaxUiScraping() {
        return ajaxUiScraping;
    }

    /**
     * Reports whether unofficial web-interface operations are enabled.
     *
     * @return {@code true} if unofficial operations are enabled
     */
    public boolean isUseUnofficialApi() {
        return useUnofficialApi;
    }
}
