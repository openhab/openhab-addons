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
package org.openhab.binding.onecta.internal.api.dto.units;

import java.util.List;
import java.util.UUID;

import com.google.gson.annotations.SerializedName;

/**
 * @author Alexander Drent - Initial contribution
 */
public class Unit {
    @SerializedName("_id")
    private String id;
    @SerializedName("id")
    private UUID initID;
    @SerializedName("type")
    private String type;
    @SerializedName("deviceModel")
    private String deviceModel;
    @SerializedName("isCloudConnectionUp")
    private GatwaySubValueBoolean IsCloudConnectionUp;
    @SerializedName("managementPoints")
    private List<ManagementPoint> managementPoints;
    @SerializedName("embeddedId")
    private String embeddedID;
    @SerializedName("timestamp")
    private String timestamp;

    public String getId() {
        return id;
    }

    public UUID getInitID() {
        return initID;
    }

    public String getType() {
        return type;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public GatwaySubValueBoolean getIsCloudConnectionUp() {
        return IsCloudConnectionUp;
    }

    public List<ManagementPoint> getManagementPoints() {
        return managementPoints;
    }

    public String getEmbeddedID() {
        return embeddedID;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public ManagementPoint findManagementPointsByType(String key) {
        return managementPoints.stream()
                .filter(managementPoint -> key.equals(managementPoint.getManagementPointType().toString())).findFirst()
                .orElse(null);
    }
}
