package org.openhab.binding.salus.internal.handler;

import java.util.Optional;
import java.util.SortedSet;

import org.openhab.binding.salus.internal.rest.Device;
import org.openhab.binding.salus.internal.rest.DeviceProperty;

public interface CloudApi {
    SortedSet<Device> findDevices();

    Optional<Device> findDevice(String dsn);

    void setValueForProperty(String dsn, String propertyName, Object value);

    SortedSet<DeviceProperty<?>> findPropertiesForDevice(String dsn);
}
