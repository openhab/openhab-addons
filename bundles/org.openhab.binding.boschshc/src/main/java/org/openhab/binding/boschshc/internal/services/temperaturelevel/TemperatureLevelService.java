package org.openhab.binding.boschshc.internal.services.temperaturelevel;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;

public class TemperatureLevelService extends BoschSHCService<@NonNull TemperatureLevelServiceState> {
    public TemperatureLevelService() {
        super("TemperatureLevel", TemperatureLevelServiceState.class);
    }
}
