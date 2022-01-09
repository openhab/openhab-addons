/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.hydrawise.internal.api.local;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HydrawiseZoneCommandBuilder} class builds a command URL string to use when sending commands to the
 * Hydrawise local controller or cloud based API server
 *
 * @author Dan Cunningham - Initial contribution
 *
 */
@NonNullByDefault
class HydrawiseZoneCommandBuilder {

    private final StringBuilder builder;

    /**
     * Construct a new {@link HydrawiseZoneCommandBuilder} class with a base URL
     *
     * @param baseURL
     */
    public HydrawiseZoneCommandBuilder(String baseURL) {
        builder = new StringBuilder(baseURL);
    }

    /**
     * Construct a new {@link HydrawiseZoneCommandBuilder} class with a base URL and API key.
     *
     * @param baseURL
     * @param apiKey
     */
    public HydrawiseZoneCommandBuilder(String baseURL, String apiKey) {
        this(baseURL);
        builder.append("&api_key=" + apiKey);
    }

    /**
     * Sets the action parameter
     *
     * @param action
     * @return {@link HydrawiseZoneCommandBuilder}
     */
    public HydrawiseZoneCommandBuilder action(String action) {
        builder.append("&action=" + action);
        return this;
    }

    /**
     * Sets the relayId parameter
     *
     * @param action
     * @return {@link HydrawiseZoneCommandBuilder}
     */
    public HydrawiseZoneCommandBuilder relayId(int relayId) {
        builder.append("&relay_id=" + relayId);
        return this;
    }

    /**
     * Sets the relay number parameter
     *
     * @param action
     * @return {@link HydrawiseZoneCommandBuilder}
     */
    public HydrawiseZoneCommandBuilder relayNumber(int number) {
        builder.append("&relay=" + number);
        return this;
    }

    /**
     * Sets the run duration parameter
     *
     * @param action
     * @return {@link HydrawiseZoneCommandBuilder}
     */
    public HydrawiseZoneCommandBuilder duration(int seconds) {
        builder.append("&custom=" + seconds);
        return this;
    }

    /**
     * Sets the controller Id parameter
     *
     * @param action
     * @return {@link HydrawiseZoneCommandBuilder}
     */
    public HydrawiseZoneCommandBuilder controllerId(int controllerId) {
        builder.append("&controller_id=" + controllerId);
        return this;
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
