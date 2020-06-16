package org.openhab.binding.boschshc.internal.services.valvetappet;

import org.openhab.binding.boschshc.internal.BoschSHCBridgeHandler;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;

public class ValveTappetService extends BoschSHCService<ValveTappetServiceState> {
    public ValveTappetService(BoschSHCBridgeHandler bridgeHandler) {
        super(bridgeHandler, "ValveTappet", ValveTappetServiceState.class);
    }
}