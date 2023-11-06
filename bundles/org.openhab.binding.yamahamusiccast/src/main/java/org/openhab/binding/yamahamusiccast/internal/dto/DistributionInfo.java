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
package org.openhab.binding.yamahamusiccast.internal.dto;

import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;

/**
 * This class represents the DistributionInfo request requested from the Yamaha model/device via the API.
 *
 * @author Lennert Coopman - Initial contribution
 */
public class DistributionInfo {

    @SerializedName("response_code")
    private String responseCode;

    @SerializedName("group_id")
    private String groupId;

    @SerializedName("role")
    private String role;

    @SerializedName("server_zone")
    private String serverZone;

    @SerializedName("client_list")
    private JsonArray clientList;

    public String getResponseCode() {
        if (responseCode == null) {
            responseCode = "";
        }
        return responseCode;
    }

    public String getGroupId() {
        if (groupId == null) {
            groupId = "";
        }
        return groupId;
    }

    public String getRole() {
        if (role == null) {
            role = "";
        }
        return role;
    }

    public String getServerZone() {
        if (serverZone == null) {
            serverZone = "";
        }
        return serverZone;
    }

    public JsonArray getClientList() {
        return clientList;
    }

    public class ClientList {
        @SerializedName("ip_address")
        private String ipaddress;

        public String getIpaddress() {
            if (ipaddress == null) {
                ipaddress = "";
            }
            return ipaddress;
        }
    }
}
