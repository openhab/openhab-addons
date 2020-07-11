package org.openhab.binding.boschshc.internal.services.temperaturelevel;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;

@NonNullByDefault
public class TemperatureLevelService extends BoschSHCService<TemperatureLevelServiceState> {
    public TemperatureLevelService() {
        super("TemperatureLevel", TemperatureLevelServiceState.class);
    }
}
