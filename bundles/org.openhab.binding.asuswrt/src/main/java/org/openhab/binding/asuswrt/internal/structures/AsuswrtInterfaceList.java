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
package org.openhab.binding.asuswrt.internal.structures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;

/**
 * The {@link AsuswrtInterfaceList} class stores a list of {@link AsuswrtIpInfo}.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtInterfaceList implements Iterable<AsuswrtIpInfo> {
    private List<AsuswrtIpInfo> ipInfoList = new ArrayList<>();

    public AsuswrtInterfaceList() {
    }

    @Override
    public Iterator<AsuswrtIpInfo> iterator() {
        return ipInfoList.iterator();
    }

    /*
     * Setters
     */

    /**
     * Adds an {@link AsuswrtIpInfo} to the list.
     */
    private void addInterface(AsuswrtIpInfo ipInfo) {
        ipInfoList.add(ipInfo);
    }

    /**
     * Sets the {@link AsuswrtInterfaceList} using a {@link JsonObject}.
     */
    public void setData(String ifName, JsonObject jsonObject) {
        if (hasInterface(ifName)) {
            getByName(ifName).setData(jsonObject);
        } else {
            addInterface(new AsuswrtIpInfo(ifName, jsonObject));
        }
    }

    /*
     * Getters
     */

    /**
     * Gets {@link AsuswrtIpInfo} from the list for an interface based on its name.
     *
     * @param ifName the name of the interface for which the info is returned
     */
    public AsuswrtIpInfo getByName(String ifName) {
        for (AsuswrtIpInfo ipInfo : ipInfoList) {
            if (ipInfo.getName().equals(ifName)) {
                return ipInfo;
            }
        }
        return new AsuswrtIpInfo();
    }

    /**
     * Gets {@link AsuswrtIpInfo} from the list for an interface based on its MAC address.
     *
     * @param ipInfoMAC the MAC address of the interface for which the info is returned
     */
    public AsuswrtIpInfo getByMAC(String ipInfoMAC) {
        for (AsuswrtIpInfo ipInfo : ipInfoList) {
            if (ipInfo.getMAC().equals(ipInfoMAC)) {
                return ipInfo;
            }
        }
        return new AsuswrtIpInfo();
    }

    /**
     * Gets {@link AsuswrtIpInfo} from the list for an interface based on its IP address.
     *
     * @param ipAddress the IP address of the interface for which the info is returned
     */
    public AsuswrtIpInfo getByIP(String ipAddress) {
        for (AsuswrtIpInfo ipInfo : ipInfoList) {
            if (ipInfo.getIpAddress().equals(ipAddress)) {
                return ipInfo;
            }
        }
        return new AsuswrtIpInfo();
    }

    /**
     * Checks if an interface with the given name is in the list.
     *
     * @param ifName the name of the interface
     */
    public boolean hasInterface(String ifName) {
        for (AsuswrtIpInfo ipInfo : ipInfoList) {
            if (ipInfo.getName().equals(ifName)) {
                return true;
            }
        }
        return false;
    }
}
