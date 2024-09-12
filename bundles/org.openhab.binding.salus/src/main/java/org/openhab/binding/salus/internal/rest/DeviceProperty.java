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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public abstract sealed class DeviceProperty<T> implements Comparable<DeviceProperty<?>> {

    private final @NotNull String name;
    private final Boolean readOnly;
    @Nullable
    private final String direction;
    @Nullable
    private final String dataUpdatedAt;
    @Nullable
    private final String productName;
    @Nullable
    private final String displayName;
    private T value;
    private final Map<String, @Nullable Object> properties;

    protected DeviceProperty(String name, @Nullable Boolean readOnly, @Nullable String direction,
            @Nullable String dataUpdatedAt, @Nullable String productName, @Nullable String displayName,
            @Nullable T value, @Nullable Map<String, @Nullable Object> properties) {
        this.name = requireNonNull(name, "name cannot be null!");
        this.readOnly = readOnly != null ? readOnly : true;
        this.direction = direction;
        this.dataUpdatedAt = dataUpdatedAt;
        this.productName = productName;
        this.displayName = displayName;
        this.value = requireNonNull(value, "value");
        this.properties = properties != null ? properties : Map.of();
    }

    public String getName() {
        return name;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    @Nullable
    public String getDirection() {
        return direction;
    }

    @Nullable
    public String getDataUpdatedAt() {
        return dataUpdatedAt;
    }

    @Nullable
    public String getProductName() {
        return productName;
    }

    @NotNull
    public String getDisplayName() {
        var dn = displayName;
        return dn != null ? dn : name;
    }

    @Nullable
    public T getValue() {
        return value;
    }

    public void setValue(@Nullable T value) {
        this.value = value;
    }

    public Map<String, @Nullable Object> getProperties() {
        return properties;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeviceProperty<?> that = (DeviceProperty<?>) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(DeviceProperty<?> o) {
        return name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return "DeviceProperty{" + "name='" + name + '\'' + ", readOnly=" + readOnly + ", direction='" + direction
                + '\'' + ", value=" + value + '}';
    }

    /**
     * @author Martin Grześlowski - Initial contribution
     */
    public static final class BooleanDeviceProperty extends DeviceProperty<Boolean> {

        protected BooleanDeviceProperty(String name, @Nullable Boolean readOnly, @Nullable String direction,
                @Nullable String dataUpdatedAt, @Nullable String productName, @Nullable String displayName,
                @Nullable Boolean value, @Nullable Map<String, @Nullable Object> properties) {
            super(name, readOnly, direction, dataUpdatedAt, productName, displayName, findValue(value), properties);
        }

        private static Boolean findValue(@Nullable Boolean value) {
            return value != null ? value : false;
        }
    }

    /**
     * @author Martin Grześlowski - Initial contribution
     */
    public static final class LongDeviceProperty extends DeviceProperty<Long> {

        protected LongDeviceProperty(String name, @Nullable Boolean readOnly, @Nullable String direction,
                @Nullable String dataUpdatedAt, @Nullable String productName, @Nullable String displayName,
                @Nullable Long value, @Nullable Map<String, @Nullable Object> properties) {
            super(name, readOnly, direction, dataUpdatedAt, productName, displayName, findValue(value), properties);
        }

        private static Long findValue(@Nullable Long value) {
            return value != null ? value : 0;
        }
    }

    /**
     * @author Martin Grześlowski - Initial contribution
     */
    public static final class StringDeviceProperty extends DeviceProperty<String> {

        protected StringDeviceProperty(String name, @Nullable Boolean readOnly, @Nullable String direction,
                @Nullable String dataUpdatedAt, @Nullable String productName, @Nullable String displayName,
                @Nullable String value, @Nullable Map<String, @Nullable Object> properties) {
            super(name, readOnly, direction, dataUpdatedAt, productName, displayName, findValue(value), properties);
        }

        private static String findValue(@Nullable String value) {
            return value != null ? value : "";
        }
    }
}
