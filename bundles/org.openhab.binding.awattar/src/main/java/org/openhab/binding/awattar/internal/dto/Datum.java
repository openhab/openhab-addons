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
package org.openhab.binding.awattar.internal.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents a Datum
 *
 * @author Wolfgang Klimt - initial contribution
 */
public class Datum {

    @SerializedName("end_timestamp")
    @Expose
    public long endTimestamp;
    @SerializedName("marketprice")
    @Expose
    public double marketprice;
    @SerializedName("start_timestamp")
    @Expose
    public long startTimestamp;
    @SerializedName("unit")
    @Expose
    public String unit;
}
