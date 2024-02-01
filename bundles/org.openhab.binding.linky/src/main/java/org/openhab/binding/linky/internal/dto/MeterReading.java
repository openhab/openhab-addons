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
package org.openhab.binding.linky.internal.dto;

import org.eclipse.jetty.jaas.spi.UserInfo;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link UserInfo} holds informations about energy delivery point
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */

public class MeterReading {
    @SerializedName("usage_point_id")
    public String usagePointId;

    @SerializedName("start")
    public String startDate;

    @SerializedName("end")
    public String endDate;

    @SerializedName("quality")
    public String quality;

    @SerializedName("reading_type")
    public ReadingType readingType;

    @SerializedName("interval_reading")
    public IntervalReading[] dayValue;

    public IntervalReading[] weekValue;
    public IntervalReading[] monthValue;
    public IntervalReading[] yearValue;
}
