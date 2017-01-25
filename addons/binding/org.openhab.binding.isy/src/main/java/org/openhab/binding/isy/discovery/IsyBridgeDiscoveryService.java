package org.openhab.binding.isy.discovery;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.isy.IsyBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsyBridgeDiscoveryService implements UpnpDiscoveryParticipant {
    private static final Logger logger = LoggerFactory.getLogger(IsyBridgeDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 30;

    // public IsyBridgeDiscoveryService() {
    // super(ImmutableSet.of(new ThingTypeUID(IsyBindingConstants.BINDING_ID, "-")), DISCOVER_TIMEOUT_SECONDS, false);
    // }
    //
    // @Override
    // protected void startScan() {
    // logger.debug("start scan called for isy bridge");
    //
    // }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(IsyBindingConstants.THING_TYPE_ISYBRIDGE);
    }

    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        // TODO Auto-generated method stub
        return null;
    }

}
