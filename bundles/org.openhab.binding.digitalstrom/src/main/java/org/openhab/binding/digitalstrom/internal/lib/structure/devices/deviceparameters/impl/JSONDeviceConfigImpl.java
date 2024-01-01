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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl;

import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.DeviceConfig;

import com.google.gson.JsonObject;

/**
 * The {@link JSONDeviceConfigImpl} is the implementation of the {@link DeviceConfig}.
 *
 * @author Alexander Betker - Initial contribution
 * @author Michael Ochel - change from SimpleJSON to GSON
 * @author Matthias Siegele - change from SimpleJSON to GSON
 */
public class JSONDeviceConfigImpl implements DeviceConfig {

    private int clazz = -1;
    private int index = -1;
    private int value = -1;

    /**
     * Creates a new {@link JSONDeviceConfigImpl}.
     *
     * @param object must not be null
     */
    public JSONDeviceConfigImpl(JsonObject object) {
        if (object.get(JSONApiResponseKeysEnum.CLASS.getKey()) != null) {
            clazz = object.get(JSONApiResponseKeysEnum.CLASS.getKey()).getAsInt();
        }
        if (object.get(JSONApiResponseKeysEnum.INDEX.getKey()) != null) {
            index = object.get(JSONApiResponseKeysEnum.INDEX.getKey()).getAsInt();
        }
        if (object.get(JSONApiResponseKeysEnum.VALUE.getKey()) != null) {
            value = object.get(JSONApiResponseKeysEnum.VALUE.getKey()).getAsInt();
        }
    }

    @Override
    public int getConfigurationClass() {
        return clazz;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "class: " + this.clazz + ", " + "index: " + this.index + ", " + "value: " + this.value;
    }
}
