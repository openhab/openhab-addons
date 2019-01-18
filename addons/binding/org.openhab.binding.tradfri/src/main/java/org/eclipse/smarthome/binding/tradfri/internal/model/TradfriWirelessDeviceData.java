/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.tradfri.internal.model;

import static org.eclipse.smarthome.binding.tradfri.TradfriBindingConstants.DEVICE_BATTERY_LEVEL;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;

import com.google.gson.JsonElement;

/**
 * The {@link TradfriWirelessDeviceData} class is a Java wrapper for the raw JSON data about wireless device state.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public abstract class TradfriWirelessDeviceData extends TradfriDeviceData {

    public TradfriWirelessDeviceData(String attributesNodeName) {
        super(attributesNodeName);
    }

    public TradfriWirelessDeviceData(String attributesNodeName, JsonElement json) {
        super(attributesNodeName, json);
    }

    public DecimalType getBatteryLevel() {
        if (generalInfo.get(DEVICE_BATTERY_LEVEL) != null) {
            return new DecimalType(generalInfo.get(DEVICE_BATTERY_LEVEL).getAsInt());
        } else {
            return null;
        }
    }

    public OnOffType getBatteryLow() {
        if (generalInfo.get(DEVICE_BATTERY_LEVEL) != null) {
            return generalInfo.get(DEVICE_BATTERY_LEVEL).getAsInt() <= 10 ? OnOffType.ON : OnOffType.OFF;
        } else {
            return null;
        }
    }
}
