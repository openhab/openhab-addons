/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.boschindego.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Battery data.
 * 
 * @author Jacob Laursen - Initial contribution
 */
public class Battery {
    public double voltage;

    public int cycles;

    public double discharge;

    @SerializedName("ambient_temp")
    public int ambientTemperature;

    @SerializedName("battery_temp")
    public int batteryTemperature;

    public int percent;
}
