package org.openhab.binding.boschshc.internal.services.temperaturelevel;

import org.openhab.binding.boschshc.internal.BoschSHCBridgeHandler;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;

public class TemperatureLevelService extends BoschSHCService<TemperatureLevelServiceState> {
    public TemperatureLevelService(BoschSHCBridgeHandler bridgeHandler) {
        super(bridgeHandler, "TemperatureLevel", TemperatureLevelServiceState.class);
    }
}