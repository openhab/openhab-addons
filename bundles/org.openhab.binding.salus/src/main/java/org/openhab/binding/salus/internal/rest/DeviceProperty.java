package org.openhab.binding.salus.internal.rest;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;

public abstract sealed class DeviceProperty<T> implements Comparable<DeviceProperty> {

    private final @NotNull String name;
    private final Boolean readOnly;
    private final String direction;
    private final String dataUpdatedAt;
    private final String productName;
    private final String displayName;
    private T value;
    private final Map<String, Object> properties;

    protected DeviceProperty(String name, Boolean readOnly, String direction, String dataUpdatedAt, String productName, String displayName, T value, Map<String, Object> properties) {
        this.name = Objects.requireNonNull(name, "name cannot be null!");
        this.readOnly = readOnly != null ? readOnly : true;
        this.direction = direction;
        this.dataUpdatedAt = dataUpdatedAt;
        this.productName = productName;
        this.displayName = displayName;
        this.value = Objects.requireNonNull(value, "value");
        this.properties = properties != null ? properties : Map.of();
    }

    public String getName() {
        return name;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public String getDirection() {
        return direction;
    }

    public String getDataUpdatedAt() {
        return dataUpdatedAt;
    }

    public String getProductName() {
        return productName;
    }

    @NotNull
    public String getDisplayName() {
        return displayName != null ? displayName : name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DeviceProperty<?> that = (DeviceProperty<?>) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(DeviceProperty o) {
        return name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return "DeviceProperty{" +
                "name='" + name + '\'' +
                ", readOnly=" + readOnly +
                ", direction='" + direction + '\'' +
                ", value=" + value +
                '}';
    }

    public static final class BooleanDeviceProperty extends DeviceProperty<Boolean> {

        protected BooleanDeviceProperty(String name, Boolean readOnly, String direction, String dataUpdatedAt, String productName, String displayName, Boolean value, Map<String, Object> properties) {
            super(name, readOnly, direction, dataUpdatedAt, productName, displayName, findValue(value), properties);
        }

        private static Boolean findValue(Boolean value) {
            return value != null ? value : false;
        }
    }

    public static final class LongDeviceProperty extends DeviceProperty<Long> {

        protected LongDeviceProperty(String name, Boolean readOnly, String direction, String dataUpdatedAt, String productName, String displayName, Long value, Map<String, Object> properties) {
            super(name, readOnly, direction, dataUpdatedAt, productName, displayName, findValue(value), properties);
        }

        private static Long findValue(Long value) {
            return value != null ? value : 0;
        }
    }

    public static final class StringDeviceProperty extends DeviceProperty<String> {

        protected StringDeviceProperty(String name, Boolean readOnly, String direction, String dataUpdatedAt, String productName, String displayName, String value, Map<String, Object> properties) {
            super(name, readOnly, direction, dataUpdatedAt, productName, displayName, findValue(value), properties);
        }

        private static String findValue(String value) {
            return value != null ? value : "";
        }
    }
}
