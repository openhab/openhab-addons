/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.tradfri.internal.model;

import static org.openhab.binding.tradfri.internal.TradfriBindingConstants.SENSOR;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonElement;

/**
 * The {@link TradfriSensorData} class is a Java wrapper for the raw JSON data about the sensor state.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class TradfriSensorData extends TradfriWirelessDeviceData {

    public TradfriSensorData(JsonElement json) {
        super(SENSOR, json);
    }
}
