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
    private String macAddress = "";
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
            this.macAddress = jsonObject.get(JSON_MEMBER_MAC).toString();
        } catch (Exception e) {
            logger.trace("incomplete SysInfo");
        }
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
        return productId;
    }

    public String getFirmwareVersion() {
        return fwVersion + " (" + this.fwBuild + ")";
    }

    public String getMAC() {
        return macAddress;
    }

    public AsuswrtUsage getMemUsage() {
        if (usageStats.containsKey(JSON_MEMBER_MEM_USAGE)) {
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
        if (usageStats.containsKey(coreKey)) {
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
