/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.sensibosky.internal.model;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Measurements} class holds the measurement data.
 *
 * @author Robert Kaczmarczyk - Initial contribution
 */
public class Measurements {
    @SerializedName("batteryVoltage")
    public String batteryVoltage;
    @SerializedName("temperature")
    public float temperature;
    @SerializedName("humidity")
    public float humidity;
}
