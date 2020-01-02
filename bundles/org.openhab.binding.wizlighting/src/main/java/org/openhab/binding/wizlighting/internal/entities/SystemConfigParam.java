/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.wizlighting.internal.entities;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This POJO represents the "params" of one WiZ Lighting System Configuration
 * The same param packet is (presumably) used for set and get.
 * When setting, it is called as a parameter, when receiving, it is called the result
 *
 * @author Sara Geleskie Damiano - Initial contribution
 *
 */
@NonNullByDefault
public class SystemConfigParam implements Param {
    // The MAC address of the bulb
    public @Nullable String mac;
    // The ID of home the bulb is assigned to
    public int homeId;
    // The ID of room the bulb is assigned to
    public int roomId;
    // Not sure what the home lock is
    public boolean homeLock;
    // Also not sure about the pairing lock
    public boolean pairingLock;
    // Obviously a type ID
    // The value is 0 for both BR30 and A19 full color bulbs
    public int typeId;
    // The module name
    // The value is "ESP01_SHRGB1C_31" for both BR30 and A19 full color bulbs
    public @Nullable String moduleName;
    // Firmware version of the bulb
    public @Nullable String fwVersion;
    // The ID of group the bulb is assigned to
    // I don't know how to group bulbs, all of mine return 0
    public int groupId;
    // Not sure what the numbers mean
    // For a full color A19 I get [33,1]
    // For a full coloer BR30 I get [37,1]
    public int drvConf[] = {};

    public @Nullable String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getHomeId() {
        return homeId;
    }

    public void setHomeId(int homeId) {
        this.homeId = homeId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public boolean isHomeLock() {
        return homeLock;
    }

    public void setHomeLock(boolean homeLock) {
        this.homeLock = homeLock;
    }

    public boolean isPairingLock() {
        return pairingLock;
    }

    public void setPairingLock(boolean pairingLock) {
        this.pairingLock = pairingLock;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public @Nullable String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public @Nullable String getFwVersion() {
        return fwVersion;
    }

    public void setFwVersion(String fwVersion) {
        this.fwVersion = fwVersion;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int[] getDrvConf() {
        return drvConf;
    }

    public void setDrvConf(int[] drvConf) {
        this.drvConf = drvConf;
    }
}
