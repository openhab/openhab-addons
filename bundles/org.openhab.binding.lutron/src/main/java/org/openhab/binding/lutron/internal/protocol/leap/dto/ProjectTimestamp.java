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
package org.openhab.binding.lutron.internal.protocol.leap.dto;

import com.google.gson.annotations.SerializedName;

/**
 * LEAP ProjectTimestamp Object
 *
 * @author Peter Wojciechowski - Initial contribution
 */
public class ProjectTimestamp {
    @SerializedName("Year")
    public int year;
    @SerializedName("Month")
    public int month;
    @SerializedName("Day")
    public int day;
    @SerializedName("Hour")
    public int hour;
    @SerializedName("Minute")
    public int minute;
    @SerializedName("Second")
    public int second;
    @SerializedName("Utc")
    public String utc;
}
