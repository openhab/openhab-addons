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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
    private Map<String, AsuswrtUsage> usageStats = new HashMap<>();

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
     * Set all data from jsonObject
     * 
     * @param jsonObject whit any data
     */
    public void setAllData(JsonObject jsonObject) {
        setSysInfo(jsonObject);
        setNetworkData(jsonObject);
        setClientData(jsonObject);
        setUsageStats(jsonObject);
    }

    /**
     * Set SysInfo from jsonObject
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
     * Set Network from jsonObject
     * 
     * @param jsonObject with network data
     */
    public void setNetworkData(JsonObject jsonObject) {
        this.lanInfo.setData(jsonObject, CHANNEL_GROUP_LANINFO);
        this.wanInfo.setData(jsonObject, CHANNEL_GROUP_WANINFO);
    }

    /**
     * Set ClientList from jsonObject
     * 
     * @param jsonObject
     */
    public void setClientData(JsonObject jsonObject) {
        this.clientList.setData(jsonObject);
    }

    /**
     * Set UsageStats from jsonObject
     * 
     * @param jsonObject
     */
    public void setUsageStats(JsonObject jsonObject) {
        JsonObject jsnMemUsage = jsonObject.getAsJsonObject(JSON_MEMBER_MEM_USAGE);
        JsonObject jsnCpuUsage = jsonObject.getAsJsonObject(JSON_MEMBER_CPU_USAGE);
        /* get memory usage */
        if (jsnMemUsage != null) {
            this.usageStats.put(JSON_MEMBER_MEM_USAGE,
                    new AsuswrtUsage(jsnMemUsage, JSON_MEMBER_MEM_TOTAL, JSON_MEMBER_MEM_USED));
        }
        /* loop cpu usages */
        if (jsnCpuUsage != null) {
            for (Integer i = 1; i <= USAGE_CPU_COUNT; i++) {
                String member = JSON_MEMBER_CPU_USAGE + "_" + i;
                String total = JSON_MEMBER_CPU_TOTAL.replace("{x}", "" + i);
                String used = JSON_MEMBER_CPU_USED.replace("{x}", "" + i);
                if (jsnCpuUsage.has(total) && jsnCpuUsage.has(used)) {
                    this.usageStats.put(member, new AsuswrtUsage(jsnCpuUsage, total, used));
                }
            }
        }
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

    public AsuswrtUsage getMemUsage() {
        if (this.usageStats.containsKey(JSON_MEMBER_MEM_USAGE)) {
            @Nullable
            AsuswrtUsage usage = this.usageStats.get(JSON_MEMBER_MEM_USAGE);
            if (usage != null) {
                return usage;
            }
        }
        return new AsuswrtUsage();
    }

    /**
     * get CPU-Usage for core number x
     * 
     * @param coreNum Number of core
     * @return AsuswrtUsage-Object for core x
     */
    public AsuswrtUsage getCpuUsage(Integer coreNum) {
        String coreKey = JSON_MEMBER_CPU_USAGE + "_" + coreNum;
        if (this.usageStats.containsKey(coreKey)) {
            @Nullable
            AsuswrtUsage usage = this.usageStats.get(coreKey);
            if (usage != null) {
                return usage;
            }
        }
        return new AsuswrtUsage();
    }

    /**
     * get CPU-Usage average over all cores
     * 
     * @return AsuswrtUsage-Object
     */
    public AsuswrtUsage getCpuAverage() {
        String coreKey;
        AsuswrtUsage coreStatsX;
        Integer total = 0, used = 0, coreNum;
        for (coreNum = 1; coreNum <= USAGE_CPU_COUNT; coreNum++) {
            coreKey = JSON_MEMBER_CPU_USAGE + "_" + coreNum;
            coreStatsX = this.usageStats.get(coreKey);
            if (coreStatsX != null) {
                total += coreStatsX.getTotal();
                used += coreStatsX.getUsed();
            }
        }
        if (coreNum > 1) {
            total = total / coreNum - 1;
            used = used / coreNum - 1;
        }
        return new AsuswrtUsage(total, used);
    }
}
