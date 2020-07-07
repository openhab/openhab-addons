package org.openhab.binding.boschshc.internal.services.valvetappet;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.boschshc.internal.services.BoschSHCServiceState;

public class ValveTappetServiceState extends BoschSHCServiceState {
    public ValveTappetServiceState() {
        super("valveTappetState");
    }

    /**
     * Current open percentage of valve tappet (0 [closed] - 100 [open]).
     */
    public Integer position;

    /**
     * Current position state of valve tappet.
     */
    public State getPositionState() {
        return new DecimalType(this.position);
    }
}