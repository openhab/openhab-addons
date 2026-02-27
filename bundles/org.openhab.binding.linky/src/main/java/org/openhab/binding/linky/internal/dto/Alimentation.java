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
package org.openhab.binding.linky.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link UserInfo} holds informations about energy delivery point
 *
 * @author Laurent Arnal - Initial contribution
 */

public class Alimentation {
    @SerializedName("serial_number")
    public String serialNumber;

    @SerializedName("connection_state")
    public String connectionState;

    @SerializedName("voltage_level")
    public String voltageLevel;

    @SerializedName("phase_count")
    public int phaseCount;

    @SerializedName("consumption_connection_power")
    public Power consumptionConnectionPower;

    @SerializedName("generation_connection_power")
    public Power generationConnectionPower;

    @SerializedName("nominal_service_voltage")
    public Voltage nominalServiceVoltage;

}
