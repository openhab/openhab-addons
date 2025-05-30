/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.senseenergy.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SenseEnergyApiMonitorStatus } is the dto for Monitor status API call
 *
 * @author Jeff James - Initial contribution
 */
public class SenseEnergyApiMonitorStatus {
    @SerializedName("device_detection")
    public SenseEnergyApiDeviceDetection deviceDetection;
    @SerializedName("monitor_info")
    public SenseEnergyApiMonitorInfo monitorInfo;
}
