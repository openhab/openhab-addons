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
package org.openhab.binding.tesla.internal.protocol.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Vehicle} is a datastructure to capture
 * variables sent by the Tesla Vehicle
 *
 * @author Karel Goderis - Initial contribution
 */
public class Vehicle {

    public String color;
    @SerializedName("display_name")
    public String displayName;
    public String id;
    @SerializedName("option_codes")
    public String optionCodes;
    @SerializedName("vehicle_id")
    public String vehicleId;
    public String vin;
    public String[] tokens;
    public String state;
    @SerializedName("remote_start_enabled")
    public boolean remoteStartEnabled;
    @SerializedName("calendar_enabled")
    public boolean calendarEnabled;
    @SerializedName("notifications_enabled")
    public boolean notificationsEnabled;
    @SerializedName("backseat_token")
    public String backseatToken;
    @SerializedName("backseat_token_updated_at")
    public String backseatTokenUpdatedAt;

    Vehicle() {
    }
}
