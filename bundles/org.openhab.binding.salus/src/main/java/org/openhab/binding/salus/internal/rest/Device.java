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
package org.openhab.binding.salus.internal.rest;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Martin Grześlowski - Initial contribution
 */
public record Device(@NotNull String dsn, @NotNull String name, boolean connected,
        @NotNull Map<@NotNull String, @Nullable Object> properties) implements Comparable<Device> {
    public Device {
        requireNonNull(dsn, "DSN is required!");
        requireNonNull(name, "name is required!");
        requireNonNull(properties, "properties is required!");

        dsn = dsn.trim();
        name = name.trim();
    }

    @Override
    public int compareTo(Device o) {
        return dsn.compareTo(o.dsn);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Device device = (Device) o;

        return dsn.equals(device.dsn);
    }

    @Override
    public int hashCode() {
        return dsn.hashCode();
    }

    @Override
    public String toString() {
        return "Device{" + "dsn='" + dsn + '\'' + ", name='" + name + '\'' + '}';
    }
}
