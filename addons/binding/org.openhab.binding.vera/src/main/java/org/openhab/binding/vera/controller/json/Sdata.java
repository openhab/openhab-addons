/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vera.controller.json;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * @author Dmitriy Ponomarev
 */
public class Sdata {
    @SerializedName("full")
    private String full;

    @SerializedName("version")
    private String version;

    @SerializedName("model")
    private String model;

    @SerializedName("zwave_heal")
    private String zwave_heal;

    @SerializedName("temperature")
    private String temperature;

    @SerializedName("serial_number")
    private String serial_number;

    @SerializedName("fwd1")
    private String fwd1;

    @SerializedName("fwd2")
    private String fwd2;

    @SerializedName("ir")
    private String ir;

    @SerializedName("irtx")
    private String irtx;

    @SerializedName("loadtime")
    private String loadtime;

    @SerializedName("dataversion")
    private String dataversion;

    @SerializedName("state")
    private String state;

    @SerializedName("comment")
    private String comment;

    @SerializedName("sections")
    private List<Section> sections;

    @SerializedName("rooms")
    private List<Room> rooms;

    @SerializedName("scenes")
    private List<Scene> scenes;

    @SerializedName("devices")
    private List<Device> devices;

    @SerializedName("categories")
    private List<Category> categories;

    public List<Room> getRooms() {
        return rooms;
    }

    public List<Scene> getScenes() {
        return scenes;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public List<Category> getCategories() {
        return categories;
    }
}
