package org.openhab.binding.boschshc.internal.services.temperaturelevel;

import org.openhab.binding.boschshc.internal.services.BoschSHCService;

public class TemperatureLevelService extends BoschSHCService<TemperatureLevelServiceState> {
    public TemperatureLevelService() {
        super("TemperatureLevel", TemperatureLevelServiceState.class);
    }
}