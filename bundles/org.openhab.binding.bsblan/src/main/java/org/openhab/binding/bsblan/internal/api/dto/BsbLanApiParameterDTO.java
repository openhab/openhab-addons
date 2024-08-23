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
package org.openhab.binding.bsblan.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link BsbLanApiParameterDTO} is responsible for storing parameter info.
 *
 * @author Peter Schraffl - Initial contribution
 */
public class BsbLanApiParameterDTO {

    public enum DataType {
        @SerializedName("0")
        DT_VALS(0), // plain value
        @SerializedName("1")
        DT_ENUM(1), // value (8/16 Bit) followed by space followed by text
        @SerializedName("2")
        DT_BITS(2), // bit value followed by bitmask followed by text
        @SerializedName("3")
        DT_WDAY(3), // weekday
        @SerializedName("4")
        DT_HHMM(4), // hour:minute
        @SerializedName("5")
        DT_DTTM(5), // date and time
        @SerializedName("6")
        DT_DDMM(6), // day and month
        @SerializedName("7")
        DT_STRN(7), // string
        @SerializedName("8")
        DT_DWHM(8); // PPS time (day of week, hour:minute)

        private final int value;

        DataType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    @SerializedName("name")
    public String name;

    @SerializedName("value")
    public String value;

    @SerializedName("unit")
    public String unit;

    @SerializedName("desc")
    public String description;

    @SerializedName("dataType")
    public DataType dataType;
}
