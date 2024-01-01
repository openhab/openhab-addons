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
package org.openhab.binding.digitalstrom.internal.lib.structure.impl;

import java.util.LinkedList;
import java.util.List;

import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.DetailedGroupInfo;
import org.openhab.binding.digitalstrom.internal.lib.structure.Zone;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.impl.DeviceImpl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link JSONZoneImpl} is the implementation of the {@link Zone}.
 *
 * @author Alexander Betker - Initial contribution
 * @author Michael Ochel - change from SimpleJSON to GSON
 * @author Matthias Siegele - change from SimpleJSON to GSON
 */
public class JSONZoneImpl implements Zone {

    private int zoneId = -1;
    private String name;

    private final List<DetailedGroupInfo> groupList;
    private final List<Device> deviceList;

    /**
     * Creates a new {@link JSONZoneImpl} through the {@link JsonObject}.
     *
     * @param jObject of the server response, must not be null
     */
    public JSONZoneImpl(JsonObject jObject) {
        this.groupList = new LinkedList<>();
        this.deviceList = new LinkedList<>();

        if (jObject.get(JSONApiResponseKeysEnum.NAME.getKey()) != null) {
            this.name = jObject.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.ID.getKey()) != null) {
            zoneId = jObject.get(JSONApiResponseKeysEnum.ID.getKey()).getAsInt();
        }
        if (zoneId == -1) {
            if (jObject.get(JSONApiResponseKeysEnum.ZONE_ID.getKey()) != null) {
                zoneId = jObject.get(JSONApiResponseKeysEnum.ZONE_ID.getKey()).getAsInt();
            }
        }
        if (jObject.get(JSONApiResponseKeysEnum.DEVICES.getKey()) instanceof JsonArray) {
            JsonArray list = (JsonArray) jObject.get(JSONApiResponseKeysEnum.DEVICES.getKey());
            for (int i = 0; i < list.size(); i++) {
                this.deviceList.add(new DeviceImpl((JsonObject) list.get(i)));
            }
        }
        if (jObject.get(JSONApiResponseKeysEnum.GROUPS.getKey()) instanceof JsonArray) {
            JsonArray groupList = (JsonArray) jObject.get(JSONApiResponseKeysEnum.GROUPS.getKey());
            for (int i = 0; i < groupList.size(); i++) {
                this.groupList.add(new JSONDetailedGroupInfoImpl((JsonObject) groupList.get(i)));
            }
        }
    }

    @Override
    public int getZoneId() {
        return zoneId;
    }

    @Override
    public synchronized void setZoneId(int id) {
        if (id > 0) {
            this.zoneId = id;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public synchronized void setName(String name) {
        this.name = name;
    }

    @Override
    public List<DetailedGroupInfo> getGroups() {
        return groupList;
    }

    @Override
    public void addGroup(DetailedGroupInfo group) {
        if (group != null) {
            synchronized (groupList) {
                if (!groupList.contains(group)) {
                    groupList.add(group);
                }
            }
        }
    }

    @Override
    public List<Device> getDevices() {
        return deviceList;
    }

    @Override
    public void addDevice(Device device) {
        if (device != null) {
            synchronized (deviceList) {
                if (!deviceList.contains(device)) {
                    deviceList.add(device);
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Zone other) {
            return (other.getZoneId() == this.getZoneId());
        }
        return false;
    }
}
