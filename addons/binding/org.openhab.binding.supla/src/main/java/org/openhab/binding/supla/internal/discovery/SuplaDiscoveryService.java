package org.openhab.binding.supla.internal.discovery;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.supla.handler.SuplaCloudBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.openhab.binding.supla.SuplaBindingConstants.ONE_CHANNEL_RELAY_THING_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.ONE_CHANNEL_RELAY_THING_TYPE;
import static org.openhab.binding.supla.SuplaBindingConstants.SUPPORTED_THING_TYPES_UIDS;

public class SuplaDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(SuplaDiscoveryService.class);
    private final SuplaCloudBridgeHandler suplaCloudBridgeHandler;

    public SuplaDiscoveryService(SuplaCloudBridgeHandler suplaCloudBridgeHandler) {
        super(SUPPORTED_THING_TYPES_UIDS, 10, false);
        this.suplaCloudBridgeHandler = suplaCloudBridgeHandler;
    }

    public void addSuplaThing(String thingType, long thingID, String label, Map<String, Object> properties) {
        addThing(suplaCloudBridgeHandler.getThing().getUID(), thingType, thingID, label, properties);
    }

    private void addThing(ThingUID bridgeUID, String thingType, long thingID, String label, Map<String, Object> properties) {
        logger.trace("Adding new Supla thing: {}", thingID);
        ThingUID thingUID = null;
        switch (thingType) {
            case ONE_CHANNEL_RELAY_THING_ID:
                logger.trace("New {}", ONE_CHANNEL_RELAY_THING_TYPE);
                thingUID = new ThingUID(ONE_CHANNEL_RELAY_THING_TYPE, bridgeUID, String.valueOf(thingID));
                break;
        }

        if (thingUID != null) {
            logger.trace("Adding new Discovery thingType: {} bridgeType: {}", thingUID, bridgeUID);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                    .withBridge(bridgeUID)
                    .withProperties(properties)
                    .withLabel(label)
                    .build();
            logger.trace("call register: {} label: {}", discoveryResult.getBindingId(), discoveryResult.getLabel());
            thingDiscovered(discoveryResult);
        } else {
            logger.debug("Discovered Thing is unsupported: type '{}'", thingID);
        }
    }

    public void activate() {
        suplaCloudBridgeHandler.registerDiscoveryService(this);
    }

    @Override
    public void deactivate() {
        suplaCloudBridgeHandler.unregisterDiscoveryService();
    }

    @Override
    protected void startScan() {
        // Scan will be done by bridge
    }
}
