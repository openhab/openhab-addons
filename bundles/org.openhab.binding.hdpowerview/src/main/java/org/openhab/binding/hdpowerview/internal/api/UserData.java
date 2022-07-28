/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal.api;

import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * User data for an HD PowerView hub
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class UserData {
    public @Nullable String hubName;
    public boolean localTimeDataSet;
    public boolean enableScheduledEvents;
    public boolean editingEnabled;
    public boolean setupCompleted;
    public @Nullable String gateway;
    public @Nullable String dns;
    public boolean staticIp;
    @SerializedName("_id")
    public @Nullable String id;
    public @Nullable Color color;
    public boolean autoBackup;
    public @Nullable String ip;
    public @Nullable String macAddress;
    public @Nullable String mask;
    public boolean wireless;
    public @Nullable HubFirmware firmware;
    public @Nullable String serialNumber;
    public @Nullable String rfIDInt;
    public @Nullable String rfID;
    public int rfStatus;
    public @Nullable Times times;
    public @Nullable String brand;
    public boolean rcUp;
    public boolean remoteConnectEnabled;

    public String getHubName() {
        return hubName != null ? new String(Base64.getDecoder().decode(hubName)) : "";
    }
}
