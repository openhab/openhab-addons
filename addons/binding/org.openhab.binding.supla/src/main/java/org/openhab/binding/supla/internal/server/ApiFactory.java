package org.openhab.binding.supla.internal.server;

public interface ApiFactory {
    TokenManager createTokenManager(SuplaCloudServer server);
}
