/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.yamahamusiccast.internal.model;

import org.eclipse.jdt.annotation.*;
import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;

/**
 * This class represents the DistributionInfo request requested from the Yamaha model/device via the API.
 *
 * @author Lennert Coopman - Initial contribution
 */
@NonNullByDefault
public class DistributionInfo {

    @SerializedName("response_code")
    private @Nullable String responseCode;

    @SerializedName("group_id")
    private @Nullable String groupId;

    @SerializedName("role")
    private @Nullable String role;

    @SerializedName("server_zone")
    private @Nullable String serverZone;

    @SerializedName("client_list")
    private @Nullable JsonArray clientList;

    public @Nullable String getResponseCode() {
        return responseCode;
    }

    public @Nullable String getGroupId() {
        return groupId;
    }

    public @Nullable String getRole() {
        return role;
    }

    public @Nullable String getServerZone() {
        return serverZone;
    }

    public @Nullable JsonArray getClientList() {
        return clientList;
    }

    @NonNullByDefault
    public class ClientList {
        @SerializedName("ip_address")
        private @Nullable String ipaddress;

        public @Nullable String getIpaddress() {
            if (ipaddress == null) {
                ipaddress = "";
            }
            return ipaddress;
        }
    }
}
