/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link AsuswrtRouterInfo} class stores the router data
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtRouterInfo {
    private final Logger logger = LoggerFactory.getLogger(AsuswrtRouterInfo.class);

    private String productId = "";
    private String fwVersion = "";
    private String fwBuild = "";
    private AsuswrtIpInfo lanInfo = new AsuswrtIpInfo();
    private AsuswrtIpInfo wanInfo = new AsuswrtIpInfo();
    private AsuswrtClientList clientList = new AsuswrtClientList();

    /**
     * INIT CLASS
     */
    public AsuswrtRouterInfo() {
    }

    /**
     * 
     * INIT CLASS
     * 
     * @param jsonObject with sysvar
     */
    public AsuswrtRouterInfo(JsonObject jsonObject) {
        setSysInfo(jsonObject);
    }

    /***********************************
     *
     * SET VALUES
     *
     ************************************/

    /**
     * Set SysInfo
     * 
     * @param jsonObject with sysvar
     */
    public void setSysInfo(JsonObject jsonObject) {
        try {
            this.productId = jsonObject.get(JSON_MEMBER_PRODUCTID).toString();
            this.fwVersion = jsonObject.get(JSON_MEMBER_FIRMWARE).toString();
            this.fwBuild = jsonObject.get(JSON_MEMBER_BUILD).toString();
            this.lanInfo.setData(jsonObject, CHANNEL_GROUP_SYSINFO);
        } catch (Exception e) {
            logger.trace("incomplete SysInfo");
        }
    }

    /**
     * Set Data from jsonObject
     * 
     * @param jsonObject
     */
    public void setData(JsonObject jsonObject) {
        this.lanInfo.setData(jsonObject, CHANNEL_GROUP_LANINFO);
        this.wanInfo.setData(jsonObject, CHANNEL_GROUP_WANINFO);
        this.clientList.setData(jsonObject);
    }

    /***********************************
     *
     * GET VALUES
     *
     ************************************/

    public String getProductId() {
        return this.productId;
    }

    public String getFirmwareVersion() {
        return this.fwVersion + " (" + this.fwBuild + ")";
    }

    public String getMAC() {
        return this.lanInfo.getMAC();
    }

    public AsuswrtIpInfo getLanInfo() {
        return this.lanInfo;
    }

    public AsuswrtIpInfo getWanInfo() {
        return this.wanInfo;
    }

    public AsuswrtClientList getClients() {
        return this.clientList;
    }
}
