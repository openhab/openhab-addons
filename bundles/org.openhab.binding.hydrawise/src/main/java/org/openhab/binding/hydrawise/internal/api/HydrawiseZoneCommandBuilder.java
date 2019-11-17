/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.hydrawise.internal.api;

/**
 *
 * @author Dan Cunningham - Initial contribution
 *
 */
class HydrawiseZoneCommandBuilder {

    private final StringBuilder builder;

    public HydrawiseZoneCommandBuilder(String baseURL) {
        builder = new StringBuilder(baseURL);
    }

    public HydrawiseZoneCommandBuilder(String baseURL, String apiKey) {
        this(baseURL);
        builder.append("&api_key=" + apiKey);
    }

    public HydrawiseZoneCommandBuilder action(String action) {
        builder.append("&action=" + action);
        return this;
    }

    public HydrawiseZoneCommandBuilder relayId(int relayId) {
        builder.append("&relay_id=" + relayId);
        return this;
    }

    public HydrawiseZoneCommandBuilder relayNumber(int number) {
        builder.append("&relay=" + number);
        return this;
    }

    public HydrawiseZoneCommandBuilder duration(int seconds) {
        builder.append("&custom=" + seconds);
        return this;
    }

    public HydrawiseZoneCommandBuilder controllerId(int controllerId) {
        builder.append("&controller_id=" + controllerId);
        return this;
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
