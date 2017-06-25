package org.openhab.binding.supla.internal.api;

import org.openhab.binding.supla.internal.supla.entities.SuplaCloudServer;

public interface ApiFactory {
    TokenManager createTokenManager(SuplaCloudServer server);
}
