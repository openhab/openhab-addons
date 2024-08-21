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
package org.openhab.binding.zoneminder.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link MonitorItemDTO} holds monitor and monitor status information.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class MonitorItemDTO {

    /**
     * Information about how the monitor is defined in Zoneminder
     */
    @SerializedName("Monitor")
    public MonitorDTO monitor;

    /**
     * Current status of the monitor in Zoneminder
     */
    @SerializedName("Monitor_Status")
    public MonitorStatusDTO monitorStatus;

    /**
     * Event counts
     */
    @SerializedName("Event_Summary")
    public EventSummaryDTO eventSummary;
}
