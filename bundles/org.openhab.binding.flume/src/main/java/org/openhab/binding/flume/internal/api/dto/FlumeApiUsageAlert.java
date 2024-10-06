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

import java.time.Instant;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link FlumeApiUsageAlert} dto for querying usage alerts.
 *
 * @author Jeff James - Initial contribution
 */
public class FlumeApiUsageAlert {
    public int id;
    @SerializedName("device_id")
    public String deviceId;
    @SerializedName("triggered_datetime")
    public Instant triggeredDateTime;
    @SerializedName("flume_leak")
    public boolean leak;
    public FlumeApiQueryWaterUsage query;
    @SerializedName("event_rule_name")
    public String eventRuleName;
}
