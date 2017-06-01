/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vera2.controller.json;

import org.openhab.binding.vera2.controller.CategoryType;

import com.google.gson.annotations.SerializedName;

/**
 * @author Dmitriy Ponomarev
 */
public class Device {
    private CategoryType categoryType;
    private String roomName;

    @SerializedName("name")
    private String name;

    @SerializedName("altid")
    private String altid;

    @SerializedName("id")
    private String id;

    @SerializedName("category")
    private String category;

    @SerializedName("subcategory")
    private String subcategory;

    @SerializedName("room")
    private String room;

    @SerializedName("parent")
    private String parent;

    @SerializedName("kwh")
    private String kwh;

    @SerializedName("watts")
    private String watts;

    @SerializedName("batterylevel")
    private String batterylevel;

    @SerializedName("locked")
    private String locked;

    @SerializedName("status")
    private String status;

    @SerializedName("level")
    private String level;

    @SerializedName("state")
    private String state;

    @SerializedName("comment")
    private String comment;

    @SerializedName("humidity")
    private String humidity;

    @SerializedName("light")
    private String light;

    @SerializedName("temperature")
    private String temperature;

    @SerializedName("tripped")
    private String tripped;

    public CategoryType getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(CategoryType categoryType) {
        this.categoryType = categoryType;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public String getRoom() {
        return room;
    }

    public String getKwh() {
        return kwh;
    }

    public String getWatts() {
        return watts;
    }

    public String getBatterylevel() {
        return batterylevel;
    }

    public String getLocked() {
        return locked;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getLight() {
        return light;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getTripped() {
        return tripped;
    }

    @Override
    public String toString() {
        return "{" + id + ", " + name + "}";
    }
}
