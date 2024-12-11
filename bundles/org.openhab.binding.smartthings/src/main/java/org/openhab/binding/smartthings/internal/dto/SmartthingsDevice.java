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

package org.openhab.binding.smartthings.internal.dto;

/**
 * Data object for smartthings app request
 *
 * @author Laurent ARNAL - Initial contribution
 */

public class SmartthingsDevice {
    public String deviceId;
    public String name;
    public String label;
    public String manufacturerName;
    public String presentationId;
    public String deviceManufacturerCode;
    public String deviceTypeName;
    public String locationId;
    public String ownerId;
    public String roomId;
    public String createTime;
    public String type;
    public int restrictionTier;
    public String executionContext;
    public String parentDeviceId;

    public record profile(String id) {
    }

    public profile profile;

    public SmartthingsComponent[] components;

    public SmartthingsDeviceViper viper;
    public SmartthingsDeviceLan lan;
    public SmartthingsDeviceOcf ocf;
    public SmartthingsDeviceHub hub;
    public SmartthingsDeviceZigBee zigbee;

    public SmartthingsDevice[] childDevices;

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("capability :").append(deviceTypeName);
        sb.append(", name: ").append(name);
        sb.append(", id: ").append(deviceId);
        return sb.toString();
    }
}
