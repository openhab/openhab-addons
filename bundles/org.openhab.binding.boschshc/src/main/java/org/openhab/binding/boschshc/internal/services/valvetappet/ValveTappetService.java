package org.openhab.binding.boschshc.internal.services.valvetappet;

import org.openhab.binding.boschshc.internal.services.BoschSHCService;

public class ValveTappetService extends BoschSHCService<ValveTappetServiceState> {
    public ValveTappetService() {
        super("ValveTappet", ValveTappetServiceState.class);
    }
}