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
package org.openhab.binding.nuki.internal.dto;

/**
 * The {@link BridgeApiListDeviceDto} class defines the Data Transfer Object (POJO) for the Nuki Bridge API /list
 * endpoint.
 *
 * @author Jan Vybíral - Initial contribution
 */
public class BridgeApiListDeviceDto {

    private String nukiId;
    private String firmwareVersion;
    private int deviceType;
    private String name;
    private BridgeApiListDeviceLastKnownState lastKnownState;

    public String getNukiId() {
        return nukiId;
    }

    public void setNukiId(String nukiId) {
        this.nukiId = nukiId;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BridgeApiListDeviceLastKnownState getLastKnownState() {
        return lastKnownState;
    }

    public void setLastKnownState(BridgeApiListDeviceLastKnownState lastKnownState) {
        this.lastKnownState = lastKnownState;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }
}
