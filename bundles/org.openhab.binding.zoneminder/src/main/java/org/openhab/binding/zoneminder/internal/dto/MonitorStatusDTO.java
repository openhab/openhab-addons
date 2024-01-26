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
 * The {@link MonitorStatusDTO} contains the current status of the monitor.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class MonitorStatusDTO {

    /**
     * Monitor Id
     */
    @SerializedName("MonitorId")
    public String monitorId;

    /**
     * Status of the monitor (e.g. Connected)
     */
    @SerializedName("Status")
    public String status;

    /**
     * Analysis frames per second
     */
    @SerializedName("AnalysisFPS")
    public String analysisFPS;

    /**
     * Video capture bandwidth
     */
    @SerializedName("CaptureBandwidth")
    public String captureBandwidth;

    /**
     * Video capture frames per second
     */
    @SerializedName("CaptureFPS")
    public String captureFPS;
}
