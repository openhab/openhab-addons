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
package org.openhab.binding.tuya.internal.cloud.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SubDeviceInfo} encapsulates the information about a device connected through a gateway
 *
 * @author Maciej Jarzebowski - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("unused")
public class SubDeviceInfo {
    public String id = "";
    public String name = "";
    public String category = "";
    public String icon = "";
    public boolean online = false;

    @SerializedName("node_id")
    public String nodeId = "";
    @SerializedName("product_id")
    public String productId = "";
    @SerializedName("owner_id")
    public String ownerId = "";

    @SerializedName("active_time")
    public long activeTime = 0;
    @SerializedName("update_time")
    public long updateTime = 0;

    @Override
    public String toString() {
        return "SubDeviceInfo{" + "id='" + id + "', name='" + name + "', category='" + category + "', icon='" + icon
                + "', online=" + online + ", nodeId='" + nodeId + "', productId='" + productId + "', ownerId='"
                + ownerId + "', activeTime=" + activeTime + ", updateTime=" + updateTime + "}";
    }
}
