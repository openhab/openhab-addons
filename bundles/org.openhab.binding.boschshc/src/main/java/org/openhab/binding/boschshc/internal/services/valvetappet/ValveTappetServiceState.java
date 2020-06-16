package org.openhab.binding.boschshc.internal.services.valvetappet;

import org.openhab.binding.boschshc.internal.services.BoschSHCServiceState;

public class ValveTappetServiceState extends BoschSHCServiceState {
    public ValveTappetServiceState() {
        super("valveTappetState");
    }

    /**
     * Current open percentage of valve tappet (0 [closed] - 100 [open]).
     */
    public Integer position;
}