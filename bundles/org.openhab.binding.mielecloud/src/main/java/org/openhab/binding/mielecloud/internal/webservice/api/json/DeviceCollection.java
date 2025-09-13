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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import java.util.HashMap;
import java.util.List;
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
    private static final java.lang.reflect.Type LIST_OF_STRING_DEVICE_MAP_TYPE = new TypeToken<List<Map<String, Device>>>() {
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
        MieleSyntaxException exception = null;

        try {
            Map<String, Device> devices = new Gson().fromJson(json, STRING_DEVICE_MAP_TYPE);
            if (devices == null) {
                exception = new MieleSyntaxException("Failed to parse Json.");
            } else {
                return new DeviceCollection(devices);
            }
        } catch (JsonSyntaxException e) {
            exception = new MieleSyntaxException("Failed to parse Json.", e);
        }

        // In September 2025 the Miele API suddenly started returning a list of mapped devices. As
        // we don't know whether this is intended we also try to parse this representation.
        try {
            List<Map<String, Device>> devices = new Gson().fromJson(json, LIST_OF_STRING_DEVICE_MAP_TYPE);
            if (devices == null) {
                // Prefer to throw the original exception as it corresponds to the documented behavior.
                throw exception;
            }

            return new DeviceCollection(devices.stream().reduce(new HashMap<>(), (a, b) -> {
                a.putAll(b);
                return a;
            }));
        } catch (JsonSyntaxException e) {
            // Prefer to throw the original exception as it corresponds to the documented behavior.
            throw exception;
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
