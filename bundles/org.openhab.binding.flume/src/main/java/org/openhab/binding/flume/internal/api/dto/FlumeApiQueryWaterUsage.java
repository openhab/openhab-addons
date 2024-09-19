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
package org.openhab.binding.flume.internal.api.dto;

import java.time.LocalDateTime;

import org.openhab.binding.flume.internal.api.FlumeApi;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link FlumeApiQueryWaterUsage} dto for setting up query of water usage.
 *
 * @author Jeff James - Initial contribution
 */
public class FlumeApiQueryWaterUsage {
    @SerializedName("request_id")
    public String requestId;
    @SerializedName("since_datetime")
    public LocalDateTime sinceDateTime;
    @SerializedName("until_datetime")
    public LocalDateTime untilDateTime;
    @SerializedName("tz")
    public String timeZone;
    public FlumeApi.BucketType bucket;
    @SerializedName("device_id")
    public String[] deviceId;
    @SerializedName("group_multiplier")
    public Integer groupMultiplier;
    public FlumeApi.OperationType operation;
    public FlumeApi.UnitType units;
    @SerializedName("sort_direction")
    public FlumeApi.SortDirectionType sortDirection;
}
