/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.lametrictime.internal.api.local.dto;

/**
 * Pojo for api.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class Api {
    private String apiVersion;
    private Endpoints endpoints;

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public Api withApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public Endpoints getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Endpoints endpoints) {
        this.endpoints = endpoints;
    }

    public Api withEndpoints(Endpoints endpoints) {
        this.endpoints = endpoints;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Api [apiVersion=");
        builder.append(apiVersion);
        builder.append(", endpoints=");
        builder.append(endpoints);
        builder.append("]");
        return builder.toString();
    }
}
