package org.openhab.binding.salus.internal.rest;

import javax.validation.constraints.NotNull;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public record Device(@NotNull String dsn, @NotNull String name,
        @NotNull Map<String, Object> properties) implements Comparable<Device> {
    public Device {
        requireNonNull(dsn, "DSN is required!");
        requireNonNull(name, "name is required!");
        requireNonNull(properties, "properties is required!");
    }

    @Override
    public int compareTo(Device o) {
        return dsn.compareTo(o.dsn);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

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
