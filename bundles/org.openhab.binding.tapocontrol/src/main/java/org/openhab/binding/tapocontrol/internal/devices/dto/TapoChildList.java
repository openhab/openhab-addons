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
package org.openhab.binding.tapocontrol.internal.devices.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Tapo-Child Structure Class
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class TapoChildList {
    @Expose
    @SerializedName("start_index")
    private int startIndex = 0;

    @Expose
    private int sum = 0;

    @Expose
    @SerializedName("child_device_list")
    private List<TapoChildDeviceData> childDeviceList = List.of();

    /***********************************
     *
     * GET VALUES
     *
     ************************************/

    public int getStartIndex() {
        return startIndex;
    }

    public int getSum() {
        return sum;
    }

    public List<TapoChildDeviceData> getChildDeviceList() {
        return childDeviceList;
    }
}
