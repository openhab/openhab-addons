package org.openhab.binding.supla.internal.api;

import org.openhab.binding.supla.internal.server.Token;

import java.util.Optional;

public interface TokenManager {
    Optional<Token> obtainToken();
}
