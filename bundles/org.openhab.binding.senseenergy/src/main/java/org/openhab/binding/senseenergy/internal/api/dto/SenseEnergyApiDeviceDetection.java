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
package org.openhab.binding.senseenergy.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SenseEnergyApiDeviceDetection }
 *
 * @author Jeff James - Initial contribution
 */
public class SenseEnergyApiDeviceDetection {
    @SerializedName("num_detected")
    public int numDetected;
}

/* @formatter:off
"device_detection": {
     "in_progress":[],
     "found":[],
     "num_detected":15
 },
@formatter:on
*/
