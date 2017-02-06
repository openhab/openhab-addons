package org.openhab.binding.nest.discovery;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.nest.NestBindingConstants;

public class NestBridgeDiscovery extends AbstractDiscoveryService {

    public NestBridgeDiscovery(int timeout) throws IllegalArgumentException {
        super(timeout);
    }

    @Override
    protected void startScan() {
        ThingUID thingUID = new ThingUID(NestBindingConstants.THING_TYPE_BRIDGE, "bridge");
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel("Nest Bridge")
                .withThingType(NestBindingConstants.THING_TYPE_BRIDGE).build();
        thingDiscovered(discoveryResult);
    }
}
