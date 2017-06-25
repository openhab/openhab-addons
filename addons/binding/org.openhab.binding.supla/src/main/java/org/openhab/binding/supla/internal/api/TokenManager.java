package org.openhab.binding.supla.internal.api;

import org.openhab.binding.supla.internal.server.SuplaToken;

import java.util.Optional;

public interface TokenManager {
    Optional<SuplaToken> obtainToken();
}
