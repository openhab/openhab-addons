package org.openhab.binding.supla.internal.api;

import org.openhab.binding.supla.internal.supla.entities.SuplaIoDevice;

import java.util.List;

public interface IoDevicesManager {
    List<SuplaIoDevice> obtainIoDevices();
}
