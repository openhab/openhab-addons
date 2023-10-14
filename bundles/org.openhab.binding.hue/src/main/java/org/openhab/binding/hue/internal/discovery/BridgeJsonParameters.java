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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link BridgeJsonParameters} class defines JSON object, which
 * contains bridge attributes like IP address. It is used for bridge
 * N-UPNP Discovery.
 *
 * @author Awelkiyar Wehabrebi - Initial contribution and API
 * @author Christoph Knauf - Refactorings
 */
@NonNullByDefault
public class BridgeJsonParameters {

    private @Nullable String id;
    @SerializedName("internalipaddress")
    private @Nullable String internalIpAddress;
    @SerializedName("macaddress")
    private @Nullable String macAddress;
    private @Nullable String name;

    @SuppressWarnings("unused")
    private BridgeJsonParameters() {
        // This no arguments constructor is required for Gson deserialization
    }

    public BridgeJsonParameters(String id, String internalIpAddress, String macAdress, String name) {
        this.id = id;
        this.internalIpAddress = internalIpAddress;
        this.macAddress = macAdress;
        this.name = name;
    }

    public @Nullable String getInternalIpAddress() {
        return internalIpAddress;
    }

    public @Nullable String getId() {
        return id;
    }

    public @Nullable String getMacAddress() {
        return macAddress;
    }

    public @Nullable String getName() {
        return name;
    }
}
