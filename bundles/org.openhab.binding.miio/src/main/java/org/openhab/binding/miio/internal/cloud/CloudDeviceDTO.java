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
package org.openhab.binding.miio.internal.cloud;

import org.eclipse.jdt.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO class wraps the device info json structure
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class CloudDeviceDTO {

    @SerializedName("did")
    @Expose
    private String did;
    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("longitude")
    @Expose
    private String longitude;
    @SerializedName("latitude")
    @Expose
    private String latitude;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("pid")
    @Expose
    private String pid;
    @SerializedName("localip")
    @Expose
    private String localip;
    @SerializedName("mac")
    @Expose
    private String mac;
    @SerializedName("ssid")
    @Expose
    private String ssid;
    @SerializedName("bssid")
    @Expose
    private String bssid;
    @SerializedName("parent_id")
    @Expose
    private String parentId;
    @SerializedName("parent_model")
    @Expose
    private String parentModel;
    @SerializedName("show_mode")
    @Expose
    private Integer showMode;
    @SerializedName("model")
    @Expose
    private String model;
    @SerializedName("adminFlag")
    @Expose
    private Integer adminFlag;
    @SerializedName("shareFlag")
    @Expose
    private Integer shareFlag;
    @SerializedName("permitLevel")
    @Expose
    private Integer permitLevel;
    @SerializedName("isOnline")
    @Expose
    private Boolean isOnline;
    @SerializedName("desc")
    @Expose
    private String desc;
    @SerializedName("uid")
    @Expose
    private Integer uid;
    @SerializedName("pd_id")
    @Expose
    private Integer pdId;
    @SerializedName("password")
    @Expose
    private String password;
    @SerializedName("rssi")
    @Expose
    private Integer rssi;
    @SerializedName("family_id")
    @Expose
    private Integer familyId;
    private @NonNull String server = "undefined";

    public @NonNull String getDid() {
        return did != null ? did : "";
    }

    public @NonNull String getToken() {
        return token != null ? token : "";
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public @NonNull String getName() {
        return name != null ? name : "";
    }

    public String getPid() {
        return pid;
    }

    public @NonNull String getLocalip() {
        return localip != null ? localip : "";
    }

    public String getMac() {
        return mac;
    }

    public String getSsid() {
        return ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public String getParentId() {
        return parentId;
    }

    public String getParentModel() {
        return parentModel;
    }

    public Integer getShowMode() {
        return showMode;
    }

    public String getModel() {
        return model;
    }

    public Integer getAdminFlag() {
        return adminFlag;
    }

    public Integer getShareFlag() {
        return shareFlag;
    }

    public Integer getPermitLevel() {
        return permitLevel;
    }

    public Boolean getIsOnline() {
        return isOnline;
    }

    public String getDesc() {
        return desc;
    }

    public Integer getUid() {
        return uid;
    }

    public Integer getPdId() {
        return pdId;
    }

    public String getPassword() {
        return password;
    }

    public Integer getRssi() {
        return rssi;
    }

    public Integer getFamilyId() {
        return familyId;
    }

    public @NonNull String getServer() {
        return server;
    }

    public void setServer(@NonNull String server) {
        this.server = server;
    }

    @Override
    public String toString() {
        return "Device name: '" + getName() + "', did: '" + getDid() + "', token: '" + getToken() + "', ip: "
                + getLocalip() + ", server: " + server;
    }
}
