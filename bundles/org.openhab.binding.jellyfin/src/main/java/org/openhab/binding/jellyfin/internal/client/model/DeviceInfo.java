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
package org.openhab.binding.jellyfin.internal.client.model;

import java.util.Objects;

/**
 * The device information is used to identify the device the client application is running on.
 *
 * @author Patrik Gfeller, based on Android SDK by Peter Feller - Initial contribution (AI generated code by "Claude
 *         Sonnet 3.7")
 */
public class DeviceInfo {

    private final String id;
    private final String name;

    /**
     * Create a new DeviceInfo instance
     *
     * @param id Unique id of the device. Only one user may be authenticated per device.
     *            It is recommended to generate a unique value for each user in the client.
     * @param name Name of the device
     */
    public DeviceInfo(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Get the device id
     *
     * @return The unique device id
     */
    public String getId() {
        return id;
    }

    /**
     * Get the device name
     *
     * @return The device name
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DeviceInfo that = (DeviceInfo) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "DeviceInfo{" + "id='" + id + '\'' + ", name='" + name + '\'' + '}';
    }
}
