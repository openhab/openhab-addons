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
package org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json;

import com.google.gson.annotations.SerializedName;

/**
 * @author Danny Baumann - Initial contribution
 */
public class StatsReport {
    @SerializedName("area")
    public int area;
    @SerializedName("time")
    public int timeInSeconds;
    @SerializedName("cid")
    public String cid;
    @SerializedName("start")
    public long startTimestamp;
    @SerializedName("type")
    public String type; // auto, ... ?
}
