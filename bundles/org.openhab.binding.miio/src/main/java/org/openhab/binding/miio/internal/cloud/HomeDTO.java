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
package org.openhab.binding.miio.internal.cloud;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO class wraps the home json structure
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class HomeDTO {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("bssid")
    @Expose
    private String bssid;
    @SerializedName("dids")
    @Expose
    private List<String> dids;
    @SerializedName("temp_dids")
    @Expose
    private Object tempDids;
    @SerializedName("icon")
    @Expose
    private String icon;
    @SerializedName("shareflag")
    @Expose
    private Integer shareflag;
    @SerializedName("permit_level")
    @Expose
    private Integer permitLevel;
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("background")
    @Expose
    private String background;
    @SerializedName("smart_room_background")
    @Expose
    private String smartRoomBackground;
    @SerializedName("longitude")
    @Expose
    private Integer longitude;
    @SerializedName("latitude")
    @Expose
    private Integer latitude;
    @SerializedName("city_id")
    @Expose
    private Integer cityId;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("create_time")
    @Expose
    private Integer createTime;
    @SerializedName("roomlist")
    @Expose
    private List<HomeRoomDTO> roomlist;
    @SerializedName("uid")
    @Expose
    private Integer uid;
    @SerializedName("appear_home_list")
    @Expose
    private Object appearHomeList;
    @SerializedName("popup_flag")
    @Expose
    private Integer popupFlag;
    @SerializedName("popup_time_stamp")
    @Expose
    private Integer popupTimeStamp;
    @SerializedName("car_did")
    @Expose
    private String carDid;

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

    public List<String> getDids() {
        return dids;
    }

    public void setDids(List<String> dids) {
        this.dids = dids;
    }

    public Object getTempDids() {
        return tempDids;
    }

    public void setTempDids(Object tempDids) {
        this.tempDids = tempDids;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getShareflag() {
        return shareflag;
    }

    public void setShareflag(Integer shareflag) {
        this.shareflag = shareflag;
    }

    public Integer getPermitLevel() {
        return permitLevel;
    }

    public void setPermitLevel(Integer permitLevel) {
        this.permitLevel = permitLevel;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getSmartRoomBackground() {
        return smartRoomBackground;
    }

    public void setSmartRoomBackground(String smartRoomBackground) {
        this.smartRoomBackground = smartRoomBackground;
    }

    public Integer getLongitude() {
        return longitude;
    }

    public void setLongitude(Integer longitude) {
        this.longitude = longitude;
    }

    public Integer getLatitude() {
        return latitude;
    }

    public void setLatitude(Integer latitude) {
        this.latitude = latitude;
    }

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    public List<HomeRoomDTO> getRoomlist() {
        return roomlist;
    }

    public void setRoomlist(List<HomeRoomDTO> roomlist) {
        this.roomlist = roomlist;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Object getAppearHomeList() {
        return appearHomeList;
    }

    public void setAppearHomeList(Object appearHomeList) {
        this.appearHomeList = appearHomeList;
    }

    public Integer getPopupFlag() {
        return popupFlag;
    }

    public void setPopupFlag(Integer popupFlag) {
        this.popupFlag = popupFlag;
    }

    public Integer getPopupTimeStamp() {
        return popupTimeStamp;
    }

    public void setPopupTimeStamp(Integer popupTimeStamp) {
        this.popupTimeStamp = popupTimeStamp;
    }

    public String getCarDid() {
        return carDid;
    }

    public void setCarDid(String carDid) {
        this.carDid = carDid;
    }
}
