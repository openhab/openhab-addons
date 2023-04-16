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
 * The {@link AsuswrtInterfaceList} class stores ipInfo list
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtInterfaceList implements Iterable<AsuswrtIpInfo> {
    private List<AsuswrtIpInfo> ipInfoList = new ArrayList<AsuswrtIpInfo>();

    /**
     * INIT CLASS
     */
    public AsuswrtInterfaceList() {
    }

    /**
     * ITERATOR
     * 
     * @return ipInfoInfo
     */
    @Override
    public Iterator<AsuswrtIpInfo> iterator() {
        return ipInfoList.iterator();
    }

    /***********************************
     *
     * SET VALUES
     *
     ************************************/

    /**
     * ADD INTERFACE TO LIST
     */
    private void addInterface(AsuswrtIpInfo ipInfo) {
        this.ipInfoList.add(ipInfo);
    }

    /**
     * Set InterfaceData from jsonObject / create new if not exists
     * 
     * @param ifName name of interface
     * @param jsonObject with interface data
     */
    public void setData(String ifName, JsonObject jsonObject) {
        if (hasInterface(ifName).equals(true)) {
            getByName(ifName).setData(jsonObject);
        } else {
            addInterface(new AsuswrtIpInfo(ifName, jsonObject));
        }
    }

    /***********************************
     *
     * GET VALUES
     *
     ************************************/

    /**
     * GET INTERFACE BY NAME
     * 
     * @param ipInfoName String interface
     * @return AsuswrtIpInfo
     */
    public AsuswrtIpInfo getByName(String ifName) {
        for (AsuswrtIpInfo ipInfo : this.ipInfoList) {
            if (ipInfo.getName().equals(ifName)) {
                return ipInfo;
            }
        }
        return new AsuswrtIpInfo();
    }

    /**
     * GET INTERFACE BY MAC
     * 
     * @param ipInfoMAC String ipInfo MAC-Address
     * @return AsuswrtIpInfo
     */
    public AsuswrtIpInfo getByMAC(String ipInfoMAC) {
        for (AsuswrtIpInfo ipInfo : this.ipInfoList) {
            if (ipInfo.getMAC().equals(ipInfoMAC)) {
                return ipInfo;
            }
        }
        return new AsuswrtIpInfo();
    }

    /**
     * GET INTERFACE BY IP
     * 
     * @param ipAddress String IP-Address
     * @return AsuswrtIpInfo
     */
    public AsuswrtIpInfo getByIP(String ipAddress) {
        for (AsuswrtIpInfo ipInfo : this.ipInfoList) {
            if (ipInfo.getIpAddress().equals(ipAddress)) {
                return ipInfo;
            }
        }
        return new AsuswrtIpInfo();
    }

    /**
     * Check if interface is in list
     * 
     * @param ifName InterfaceName
     * @return true if data was set
     */
    public Boolean hasInterface(String ifName) {
        for (AsuswrtIpInfo ipInfo : this.ipInfoList) {
            if (ipInfo.getName().equals(ifName)) {
                return true;
            }
        }
        return false;
    }
}
