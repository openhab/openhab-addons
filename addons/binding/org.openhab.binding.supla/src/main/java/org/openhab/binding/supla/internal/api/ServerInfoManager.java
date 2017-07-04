package org.openhab.binding.supla.internal.api;

import org.openhab.binding.supla.internal.supla.entities.SuplaServerInfo;

import java.util.Optional;

public interface ServerInfoManager {
    Optional<SuplaServerInfo> obtainServerInfo();
}
