package org.openhab.binding.supla.internal.api;

import org.openhab.binding.supla.internal.supla.entities.SuplaIoDevice;

import java.util.List;
import java.util.Optional;

public interface IoDevicesManager {
    List<SuplaIoDevice> obtainIoDevices();

    Optional<SuplaIoDevice> obtainIoDevice(long id);
}
