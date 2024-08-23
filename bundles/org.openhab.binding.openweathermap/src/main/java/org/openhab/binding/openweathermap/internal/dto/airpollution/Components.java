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
package org.openhab.binding.openweathermap.internal.dto.airpollution;

import com.google.gson.annotations.SerializedName;

/**
 * Holds the data from the <code>components</code> object of the JSON response of the Air Pollution API.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class Components {
    @SerializedName("co")
    public double carbonMonoxide;
    @SerializedName("no")
    public double nitrogenMonoxide;
    @SerializedName("no2")
    public double nitrogenDioxide;
    @SerializedName("o3")
    public double ozone;
    @SerializedName("so2")
    public double sulphurDioxide;
    @SerializedName("pm2_5")
    public double particulateMatter2dot5;
    @SerializedName("pm10")
    public double particulateMatter10;
    @SerializedName("nh3")
    public double ammonia;
}
