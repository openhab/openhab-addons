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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * Immutable POJO representing a collection of devices queried from the Miele REST API.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class DeviceCollection {
    private static final java.lang.reflect.Type STRING_DEVICE_MAP_TYPE = new TypeToken<Map<String, Device>>() {
    }.getType();

    private final Map<String, Device> devices;

    DeviceCollection(Map<String, Device> devices) {
        this.devices = devices;
    }

    /**
     * Creates a new {@link DeviceCollection} from the given Json text.
     *
     * @param json The Json text.
     * @return The created {@link DeviceCollection}.
     * @throws MieleSyntaxException if parsing the data from {@code json} fails.
     */
    public static DeviceCollection fromJson(String json) {
        try {
            Map<String, Device> devices = new Gson().fromJson(json, STRING_DEVICE_MAP_TYPE);
            if (devices == null) {
                throw new MieleSyntaxException("Failed to parse Json.");
            }
            return new DeviceCollection(devices);
        } catch (JsonSyntaxException e) {
            throw new MieleSyntaxException("Failed to parse Json.", e);
        }
    }

    public Set<String> getDeviceIdentifiers() {
        return devices.keySet();
    }

    public Device getDevice(String identifier) {
        Device device = devices.get(identifier);
        if (device == null) {
            throw new IllegalArgumentException("There is no device for identifier " + identifier);
        }
        return device;
    }

    @Override
    public int hashCode() {
        return Objects.hash(devices);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DeviceCollection other = (DeviceCollection) obj;
        return Objects.equals(devices, other.devices);
    }

    @Override
    public String toString() {
        return "DeviceCollection [devices=" + devices + "]";
    }
}
