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
public class HydrawiseZoneCommandBuilder {

    private StringBuilder builder;

    protected HydrawiseZoneCommandBuilder(String baseURL) {
        builder = new StringBuilder(baseURL);
    }

    protected HydrawiseZoneCommandBuilder(String baseURL, String apiKey) {
        builder = new StringBuilder(baseURL);
        builder.append("&api_key=" + apiKey);
    }

    protected HydrawiseZoneCommandBuilder action(String action) {
        builder.append("&action=" + action);
        return this;
    }

    protected HydrawiseZoneCommandBuilder relayId(int relayId) {
        builder.append("&relay_id=" + relayId);
        return this;
    }

    protected HydrawiseZoneCommandBuilder relayNumber(int number) {
        builder.append("&relay=" + number);
        return this;
    }

    protected HydrawiseZoneCommandBuilder duration(int seconds) {
        builder.append("&custom=" + seconds);
        return this;
    }

    protected HydrawiseZoneCommandBuilder controllerId(int controllerId) {
        builder.append("&controller_id=" + controllerId);
        return this;
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
