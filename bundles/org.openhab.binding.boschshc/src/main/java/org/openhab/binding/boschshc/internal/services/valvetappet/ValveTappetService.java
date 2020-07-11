package org.openhab.binding.boschshc.internal.services.valvetappet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;

@NonNullByDefault
public class ValveTappetService extends BoschSHCService<ValveTappetServiceState> {
    public ValveTappetService() {
        super("ValveTappet", ValveTappetServiceState.class);
    }
}
