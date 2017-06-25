package org.openhab.binding.supla.internal.api;

import org.openhab.binding.supla.internal.supla.entities.SuplaToken;

import java.util.Optional;

public interface TokenManager {
    SuplaToken obtainToken();
}
