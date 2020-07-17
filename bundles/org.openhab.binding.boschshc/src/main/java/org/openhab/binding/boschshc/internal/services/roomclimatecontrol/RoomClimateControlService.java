package org.openhab.binding.boschshc.internal.services.roomclimatecontrol;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;

/**
 * Service of a virtual device which controls the radiator thermostats in a room.
 * 
 * @author Christian Oeing (christian.oeing@slashgames.org)
 */
@NonNullByDefault
public class RoomClimateControlService extends BoschSHCService<@NonNull RoomClimateControlServiceState> {
    /**
     * Constructor.
     */
    public RoomClimateControlService() {
        super("RoomClimateControl", RoomClimateControlServiceState.class);
    }
}