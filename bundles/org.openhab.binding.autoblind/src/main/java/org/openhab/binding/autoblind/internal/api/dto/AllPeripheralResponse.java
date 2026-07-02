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
package org.openhab.binding.autoblind.internal.api.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Response from the /NM/v1/GetAllPeripheral endpoint.
 *
 * @author Stephen Berg (@BiloxiGeek) - Initial contribution
 */
public class AllPeripheralResponse {

    @SerializedName("status")
    public StatusField status = new StatusField();

    @SerializedName("results")
    public Results results = new Results();

    public static class StatusField {
        @SerializedName("code")
        public int code;

        @SerializedName("error")
        public String error = "";
    }

    public static class Results {
        @SerializedName("ThingName")
        public String thingName = "";

        @SerializedName("CustomDeviceName")
        public String customDeviceName = "";

        @SerializedName("TimeZone")
        public String timeZone = "";

        @SerializedName("RoomList")
        public List<Room> roomList = List.of();
    }

    public static class Room {
        @SerializedName("RoomID")
        public String roomId = "";

        @SerializedName("RoomName")
        public String roomName = "";

        @SerializedName("GroupList")
        public List<Group> groupList = List.of();
    }

    public static class Group {
        @SerializedName("GroupID")
        public String groupId = "";

        @SerializedName("GroupName")
        public String groupName = "";

        @SerializedName("PeripheralList")
        public List<PeripheralInfo> peripheralList = List.of();
    }
}
