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
package org.openhab.binding.digitalstrom.internal.lib.structure.impl;

import java.util.LinkedList;
import java.util.List;

import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.DetailedGroupInfo;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link JSONDetailedGroupInfoImpl} is the implementation of the {@link DetailedGroupInfo}.
 *
 * @author Alexander Betker - Initial contribution
 * @author Michael Ochel - change from SimpleJSON to GSON
 * @author Matthias Siegele - change from SimpleJSON to GSON
 */
public class JSONDetailedGroupInfoImpl implements DetailedGroupInfo {

    private String name;
    private short groupId = 0;
    private final List<String> deviceList;

    /**
     * Creates a new {@link JSONDetailedGroupInfoImpl} through the {@link JsonObject}.
     *
     * @param jObject of the server response, must not be null
     */
    public JSONDetailedGroupInfoImpl(JsonObject jObject) {
        this.deviceList = new LinkedList<>();
        if (jObject.get(JSONApiResponseKeysEnum.NAME.getKey()) != null) {
            name = jObject.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.ID.getKey()) != null) {
            this.groupId = jObject.get(JSONApiResponseKeysEnum.ID.getKey()).getAsShort();
        }
        if (jObject.get(JSONApiResponseKeysEnum.DEVICES.getKey()) instanceof JsonArray) {
            JsonArray array = (JsonArray) jObject.get(JSONApiResponseKeysEnum.DEVICES.getKey());

            for (int i = 0; i < array.size(); i++) {
                if (array.get(i) != null) {
                    deviceList.add(array.get(i).getAsString());
                }
            }
        }
    }

    @Override
    public short getGroupID() {
        return groupId;
    }

    @Override
    public String getGroupName() {
        return name;
    }

    @Override
    public List<String> getDeviceList() {
        return deviceList;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DetailedGroupInfo group) {
            return group.getGroupID() == this.getGroupID();
        }
        return false;
    }
}
