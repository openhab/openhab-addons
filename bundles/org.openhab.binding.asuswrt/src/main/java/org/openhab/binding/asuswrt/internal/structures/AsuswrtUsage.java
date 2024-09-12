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
package org.openhab.binding.asuswrt.internal.structures;

import static org.openhab.binding.asuswrt.internal.helpers.AsuswrtUtils.jsonObjectToInt;

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

    public AsuswrtUsage() {
    }

    /**
     * Constructor.
     *
     * @param jsonObject jsonObject data is stored
     * @param totalKey name of key total available is stored
     * @param usedKey name of key used is stored
     */
    public AsuswrtUsage(JsonObject jsonObject, String totalKey, String usedKey) {
        setData(jsonObject, totalKey, usedKey);
    }

    /**
     * Constructor.
     *
     * @param totalUsage the total usage
     * @param used the usage
     */
    public AsuswrtUsage(Integer totalUsage, Integer used) {
        setData(totalUsage, used);
    }

    /*
     * Setters
     */

    /**
     * Sets the usage data from a JSON object.
     *
     * @param jsonObject the JSON object containing the data
     * @param totalKey the key name with the 'total available' value
     * @param usedKey the key name with the 'used' value
     */
    public void setData(JsonObject jsonObject, String totalKey, String usedKey) {
        total = jsonObjectToInt(jsonObject, totalKey, 0);
        used = jsonObjectToInt(jsonObject, usedKey, 0);
        free = total - used;
    }

    /**
     * Sets usage data from integer values.
     *
     * @param totalUsage the total available value
     * @param used the used value
     */
    public void setData(Integer totalUsage, Integer used) {
        total = totalUsage;
        this.used = used;
        free = total - used;
    }

    /*
     * Getters
     */

    public Integer getTotal() {
        return total;
    }

    public Integer getUsed() {
        return used;
    }

    public Integer getFree() {
        return free;
    }

    public Integer getUsedPercent() {
        if (total > 0) {
            return ((used * 100) / total);
        }
        return 0;
    }

    public Integer getFreePercent() {
        if (total > 0) {
            return ((free * 100) / total);
        }
        return 0;
    }
}
