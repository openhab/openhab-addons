/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.data;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

/**
 * Default properties shared across all Nest devices.
 *
 * @author David Bennett
 */
public class BaseNestDevice implements NestIdentifiable {
    @SerializedName("device_id")
    private String deviceId;
    @SerializedName("name")
    private String name;
    @SerializedName("name_long")
    private String nameLong;
    @SerializedName("last_connection")
    private Date lastConnection;
    @SerializedName("is_online")
    private boolean isOnline;
    @SerializedName("software_version")
    private String softwareVersion;
    @SerializedName("structure_id")
    private String structureId;
    @SerializedName("where_id")
    private String whereId;

    @Override
    public String getId() {
        return deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Date getLastConnection() {
        return lastConnection;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public String getNameLong() {
        return nameLong;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public String getStructureId() {
        return structureId;
    }

    public String getWhereId() {
        return whereId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BaseNestDevice [deviceId=").append(deviceId).append(", name=").append(name)
                .append(", nameLong=").append(nameLong).append(", lastConnection=").append(lastConnection)
                .append(", isOnline=").append(isOnline).append(", softwareVersion=").append(softwareVersion)
                .append(", structureId=").append(structureId).append(", whereId=").append(whereId).append("]");
        return builder.toString();
    }

}
