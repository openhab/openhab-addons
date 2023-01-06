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
package org.openhab.binding.hue.internal.discovery;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link BridgeJsonParameters} class defines JSON object, which
 * contains bridge attributes like IP address. It is used for bridge
 * N-UPNP Discovery.
 *
 * @author Awelkiyar Wehabrebi - Initial contribution and API
 * @author Christoph Knauf - Refactorings
 */
public class BridgeJsonParameters {

    private String id;
    @SerializedName("internalipaddress")
    private String internalIpAddress;
    @SerializedName("macaddress")
    private String macAddress;
    private String name;

    private BridgeJsonParameters() {
        // This no arguments constructor is required for Gson deserialization
    }

    public BridgeJsonParameters(String id, String internalIpAddress, String macAdress, String name) {
        this.id = id;
        this.internalIpAddress = internalIpAddress;
        this.macAddress = macAdress;
        this.name = name;
    }

    public String getInternalIpAddress() {
        return internalIpAddress;
    }

    public String getId() {
        return id;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getName() {
        return name;
    }
}
