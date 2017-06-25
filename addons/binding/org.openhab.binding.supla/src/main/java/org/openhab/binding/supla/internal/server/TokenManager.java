package org.openhab.binding.supla.internal.server;

import java.util.Optional;

public interface TokenManager {
    Optional<Token> obtainToken();
}
