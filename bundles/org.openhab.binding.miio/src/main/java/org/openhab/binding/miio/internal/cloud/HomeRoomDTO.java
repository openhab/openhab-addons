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

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO class wraps the home json structure
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class HomeRoomDTO {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("bssid")
    @Expose
    private String bssid;
    @SerializedName("parentid")
    @Expose
    private String parentid;
    @SerializedName("dids")
    @Expose
    private List<String> dids;
    @SerializedName("icon")
    @Expose
    private String icon;
    @SerializedName("background")
    @Expose
    private String background;
    @SerializedName("shareflag")
    @Expose
    private Integer shareflag;
    @SerializedName("create_time")
    @Expose
    private Integer createTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getParentid() {
        return parentid;
    }

    public void setParentid(String parentid) {
        this.parentid = parentid;
    }

    public List<String> getDids() {
        return dids;
    }

    public void setDids(List<String> dids) {
        this.dids = dids;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public Integer getShareflag() {
        return shareflag;
    }

    public void setShareflag(Integer shareflag) {
        this.shareflag = shareflag;
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }
}
