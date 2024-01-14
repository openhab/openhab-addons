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
package org.openhab.binding.neohub.internal;

import java.math.BigDecimal;
import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 * A wrapper around the JSON response to the JSON GET_ENGINEERS request
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class NeoHubGetEngineersData {

    private static final Gson GSON = new Gson();

    @SuppressWarnings("null")
    @NonNullByDefault
    private static class EngineersRecords extends HashMap<String, EngineersRecord> {
        private static final long serialVersionUID = 1L;
    }

    @SuppressWarnings("null")
    @NonNullByDefault
    public class EngineersRecord {
        @SerializedName("DEVICE_TYPE")
        private @Nullable BigDecimal deviceType;

        public int getDeviceType() {
            BigDecimal deviceType = this.deviceType;
            return deviceType != null ? deviceType.intValue() : -1;
        }
    }

    private @Nullable EngineersRecords deviceRecords;

    /**
     * Create wrapper around a JSON string
     * 
     * @param fromJson the JSON string
     * @throws JsonSyntaxException
     * 
     */
    public NeoHubGetEngineersData(String fromJson) throws JsonSyntaxException {
        deviceRecords = GSON.fromJson(fromJson, EngineersRecords.class);
    }

    public static @Nullable NeoHubGetEngineersData createEngineersData(String fromJson) throws JsonSyntaxException {
        return new NeoHubGetEngineersData(fromJson);
    }

    /**
     * returns the device record corresponding to a given device name
     * 
     * @param deviceName the device name
     * @return its respective device information record
     */
    private @Nullable EngineersRecord getDevice(String deviceName) {
        EngineersRecords deviceRecords = this.deviceRecords;
        return deviceRecords != null ? deviceRecords.get(deviceName) : null;
    }

    /**
     * returns the deviceType corresponding to a given device name
     * 
     * @param deviceName the device name
     * @return its respective device information record
     */
    public int getDeviceType(String deviceName) {
        EngineersRecord record = getDevice(deviceName);
        return record != null ? record.getDeviceType() : -1;
    }
}
