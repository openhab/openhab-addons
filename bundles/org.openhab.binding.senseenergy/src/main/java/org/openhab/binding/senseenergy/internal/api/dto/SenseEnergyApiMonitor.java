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

import java.time.Instant;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SenseEnergyApiMonitor} MonitorDevice dto structure. All fields are documented here for reference, however
 * fields
 * marked as transient are not serialized in order to save processing time.
 *
 * @author Jeff James - Initial contribution
 */
public class SenseEnergyApiMonitor {
    public long id;
    @SerializedName("date_created")
    public Instant dateCreated;
    @SerializedName("time_zone")
    public String timezone;
    @SerializedName("solar_connected")
    public boolean solarConnected;
    @SerializedName("solar_configured")
    public boolean solarConfigured;
    @SerializedName("signal_check_completed_time")
    public Instant signalCheckCompletedTime;
    @SerializedName("ethernet_supported")
    public transient boolean ethernetSupported;
    @SerializedName("power_over_ethernet_supported")
    public transient boolean powerOverEthernetSupported;
    @SerializedName("aux_ignore")
    public transient boolean auxIgnore;
    @SerializedName("aux_port")
    public String auxPort;
    @SerializedName("hardware_type")
    public String hardwareType;
    @SerializedName("zigbee_supported")
    public transient boolean zigbeeSupported;
}
