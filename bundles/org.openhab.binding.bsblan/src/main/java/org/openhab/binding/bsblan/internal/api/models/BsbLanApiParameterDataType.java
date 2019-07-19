/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.bsblan.internal.api.models;

/**
 * The {@link BsbLanApiParameterDataType} reflects the dataType field used in parameter queries.
 *
 * @author Peter Schraffl - Initial contribution
 */
public enum BsbLanApiParameterDataType {
    DT_VALS(0),    // plain value
    DT_ENUM(1),    // value (8/16 Bit) followed by space followed by text
    DT_BITS(2),    // bit value followed by bitmask followed by text
    DT_WDAY(3),    // weekday
    DT_HHMM(4),    // hour:minute
    DT_DTTM(5),    // date and time
    DT_DDMM(6),    // day and month
    DT_STRN(7),    // string
    DT_DWHM(8);    // PPS time (day of week, hour:minute)

    private final int id;
    BsbLanApiParameterDataType(int id)
    {
        this.id = id;
    }

    public int getValue()
    {
        return id;
    }
}
