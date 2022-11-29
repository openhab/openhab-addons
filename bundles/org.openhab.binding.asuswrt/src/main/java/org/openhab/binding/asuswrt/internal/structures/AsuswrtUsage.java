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

import static org.openhab.binding.asuswrt.internal.helpers.AsuswrtUtils.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;

/**
 * The {@link AsuswrtUsage} class handles usage statistics
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtUsage {
    private Integer free = 0;
    private Integer used = 0;
    private Integer total = 0;

    /**
     * INIT CLASS
     */
    public AsuswrtUsage() {
    }

    /**
     * 
     * INIT CLASS
     * 
     * @param jsonObject jsonObject data is stored
     * @param totalKey name of key total available is stored
     * @param usedKey neme of key used is stored
     */
    public AsuswrtUsage(JsonObject jsonObject, String totalKey, String usedKey) {
        setData(jsonObject, totalKey, usedKey);
    }

    /**
     * INIT CLASS
     * 
     * @param totalUsage Integer total available
     * @param used Integer used
     */
    public AsuswrtUsage(Integer totalUsage, Integer used) {
        setData(totalUsage, used);
    }

    /***********************************
     *
     * SET VALUES
     *
     ************************************/

    /**
     * set usage data from json object
     * 
     * @param jsonObject jsonObject data is stored
     * @param totalKey name of key total available is stored
     * @param usedKey neme of key used is stored
     */
    public void setData(JsonObject jsonObject, String totalKey, String usedKey) {
        this.total = jsonObjectToInt(jsonObject, totalKey, 0);
        this.used = jsonObjectToInt(jsonObject, usedKey, 0);
        this.free = total - used;
    }

    /**
     * set usage data from integer values
     * 
     * @param totalUsage Integer total available
     * @param used Integer used
     */
    public void setData(Integer totalUsage, Integer used) {
        this.total = totalUsage;
        this.used = used;
        this.free = total - used;
    }

    /***********************************
     *
     * GET VALUES
     *
     ************************************/

    public Integer getTotal() {
        return this.total;
    }

    public Integer getUsed() {
        return this.used;
    }

    public Integer getFree() {
        return this.free;
    }

    public Integer getUsedPercent() {
        if (this.total > 0) {
            return ((this.used * 100) / this.total);
        }
        return 0;
    }

    public Integer getFreePercent() {
        if (this.total > 0) {
            return ((this.free * 100) / this.total);
        }
        return 0;
    }
}
