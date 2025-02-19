/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link DeviceListInfo} encapsulates the information in the device list
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("unused")
public class DeviceListInfo {
    public String id = "";
    public String uuid = "";
    public String uid = "";

    @SerializedName("biz_type")
    public int bizType = -1;
    public String name = "";
    @SerializedName("time_zone")
    public String timeZone = "";
    public String ip = "";
    @SerializedName("local_key")
    public String localKey = "";
    @SerializedName("sub")
    public boolean subDevice = false;
    public String model = "";

    @SerializedName("create_time")
    public long createTime = 0;
    @SerializedName("update_time")
    public long updateTime = 0;
    @SerializedName("active_time")
    public long activeTime = 0;

    public List<StatusInfo> status = List.of();

    @SerializedName("owner_id")
    public String ownerId = "";
    @SerializedName("product_id")
    public String productId = "";
    @SerializedName("product_name")
    public String productName = "";

    public String category = "";
    public String icon = "";
    public boolean online = false;

    @SerializedName("node_id")
    public String nodeId = "";

    @Override
    public String toString() {
        return "DeviceListInfo{" + "id='" + id + "', uuid='" + uuid + "', uid='" + uid + "', bizType=" + bizType
                + ", name='" + name + "', timeZone='" + timeZone + "', ip='" + ip + "', localKey='" + localKey
                + "', subDevice=" + subDevice + ", model='" + model + "', createTime=" + createTime + ", updateTime="
                + updateTime + ", activeTime=" + activeTime + ", status=" + status + ", ownerId='" + ownerId
                + "', productId='" + productId + "', productName='" + productName + "', category='" + category
                + "', icon='" + icon + "', online=" + online + ", nodeId='" + nodeId + "'}";
    }
}
