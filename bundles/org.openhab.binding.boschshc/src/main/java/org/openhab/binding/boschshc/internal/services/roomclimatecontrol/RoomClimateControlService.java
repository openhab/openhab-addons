package org.openhab.binding.boschshc.internal.services.roomclimatecontrol;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;

@NonNullByDefault
public class RoomClimateControlService extends BoschSHCService<@NonNull RoomClimateControlServiceState> {
    public RoomClimateControlService() {
        super("RoomClimateControl", RoomClimateControlServiceState.class);
    }

}