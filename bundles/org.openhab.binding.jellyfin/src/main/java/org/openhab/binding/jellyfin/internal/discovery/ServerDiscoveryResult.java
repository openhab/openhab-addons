/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Patrik Gfeller - Initial Contribution
 */
@NonNullByDefault
class ServerDiscoveryResult {
    private String address;
    private String id;
    private String name;
    private String endpointAddress;

    ServerDiscoveryResult(String address, String id, String name, String endpointAddress) {
        this.address = address;
        this.id = id;
        this.name = name;
        this.endpointAddress = endpointAddress;
    }

    public String getAddress() {
        return address;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEndpointAddress() {
        return endpointAddress;
    }

    /**
     * Parse JSON string into a ServerDiscoveryResult object
     * 
     * @param jsonData The JSON formatted string to parse
     * @return ServerDiscoveryResult instance with parsed values
     */
    public static ServerDiscoveryResult fromJson(String jsonData) {
        JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();

        String address = jsonObject.get("Address").getAsString();
        String id = jsonObject.get("Id").getAsString();
        String name = jsonObject.get("Name").getAsString();
        String endpointAddress = jsonObject.has("EndpointAddress") && !jsonObject.get("EndpointAddress").isJsonNull()
                ? jsonObject.get("EndpointAddress").getAsString()
                : null;

        return new ServerDiscoveryResult(address, id, name, endpointAddress);
    }
}
