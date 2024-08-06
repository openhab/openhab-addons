package org.openhab.binding.meteofrance.internal.handler;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.binding.BridgeHandler;

@NonNullByDefault
public interface MeteoFranceChildHandler {
    default Optional<MeteoFranceBridgeHandler> getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler instanceof MeteoFranceBridgeHandler maHandler) {
                return Optional.of(maHandler);
            }
        }
        return Optional.empty();
    }

    @Nullable
    Bridge getBridge();
}
