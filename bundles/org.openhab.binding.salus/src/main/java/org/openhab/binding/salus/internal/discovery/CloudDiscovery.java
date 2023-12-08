package org.openhab.binding.salus.internal.discovery;

import org.openhab.binding.salus.internal.handler.CloudBridgeHandler;
import org.openhab.binding.salus.internal.rest.Device;
import org.openhab.binding.salus.internal.rest.SalusApi;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.openhab.binding.salus.internal.SalusBindingConstants.SALUS_DEVICE_TYPE;
import static org.openhab.binding.salus.internal.SalusBindingConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.salus.internal.SalusBindingConstants.SalusDevice.DSN;

public class CloudDiscovery extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(CloudDiscovery.class);
    private final CloudBridgeHandler bridgeHandler;

    public CloudDiscovery(CloudBridgeHandler bridgeHandler, SalusApi salusApi) throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES_UIDS, 10, true);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected void startScan() {
        logger.debug("Start Salus discovery");
        try {
            var response = bridgeHandler.getSalusApi().findDevices();
            if (response.failed()) {
                logger.error("Error while scanning: {}", response.error());
                return;
            }
            logger.debug("Found {} devices while scanning", response.body().size());
            response.body().forEach(this::addThing);
        } catch (Exception e) {
            logger.error("Error while scanning", e);
        }
        logger.debug("Finished Salus discovery");
    }

    private void addThing(Device device) {
        logger.debug("Adding device \"{}\" ({}) to found things", device.name(), device.dsn());
        var thingUID = new ThingUID(SALUS_DEVICE_TYPE, findBridgeUID(), device.dsn());
        var discoveryResult = createDiscoveryResult(thingUID, buildThingLabel(device), buildThingProperties(device));
        thingDiscovered(discoveryResult);
    }

    private ThingUID findBridgeUID() {
        return bridgeHandler.getThing().getUID();
    }

    private DiscoveryResult createDiscoveryResult(ThingUID thingUID, String label, Map<String, Object> properties) {
        return DiscoveryResultBuilder.create(thingUID)
                .withBridge(findBridgeUID())
                .withProperties(properties)
                .withLabel(label)
                .build();
    }

    private String buildThingLabel(Device device) {
        var sb = new StringBuilder();
        {
            var name = device.name();
            if (!isEmpty(name)) {
                sb.append(name);
            } else {
                sb.append(device.dsn());
            }
        }
        return sb.toString();
    }

    private Map<String, Object> buildThingProperties(Device device) {
        return Map.of(DSN, device.dsn());
    }
}
