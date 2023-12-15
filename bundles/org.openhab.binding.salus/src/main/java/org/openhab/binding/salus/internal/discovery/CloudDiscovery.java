package org.openhab.binding.salus.internal.discovery;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.openhab.binding.salus.internal.SalusBindingConstants.*;
import static org.openhab.binding.salus.internal.SalusBindingConstants.SalusDevice.DSN;

import java.util.Locale;
import java.util.Map;

import org.openhab.binding.salus.internal.handler.CloudApi;
import org.openhab.binding.salus.internal.handler.CloudBridgeHandler;
import org.openhab.binding.salus.internal.rest.Device;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudDiscovery extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(CloudDiscovery.class);
    private final CloudApi cloudApi;
    private final ThingUID bridgeUid;

    public CloudDiscovery(CloudBridgeHandler bridgeHandler, CloudApi cloudApi, ThingUID bridgeUid)
            throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES_UIDS, 10, true);
        this.cloudApi = cloudApi;
        this.bridgeUid = bridgeUid;
    }

    @Override
    protected void startScan() {
        logger.debug("Start Salus discovery");
        try {
            var devices = cloudApi.findDevices();
            logger.debug("Found {} devices while scanning", devices.size());
            devices.stream().filter(Device::isConnected).forEach(this::addThing);
        } catch (Exception e) {
            logger.error("Error while scanning", e);
        }
        logger.debug("Finished Salus discovery");
    }

    private void addThing(Device device) {
        logger.debug("Adding device \"{}\" ({}) to found things", device.name(), device.dsn());
        var thingUID = new ThingUID(findDeviceType(device), bridgeUid, device.dsn());
        var discoveryResult = createDiscoveryResult(thingUID, buildThingLabel(device), buildThingProperties(device));
        thingDiscovered(discoveryResult);
    }

    private static ThingTypeUID findDeviceType(Device device) {
        var props = device.properties();
        if (props.containsKey("oem_model")) {
            var model = props.get("oem_model");
            if (model != null) {
                if (model.toString().toLowerCase(Locale.ENGLISH).contains("it600")) {
                    return SALUS_IT600_DEVICE_TYPE;
                }
            }
        }
        return SALUS_DEVICE_TYPE;
    }

    private DiscoveryResult createDiscoveryResult(ThingUID thingUID, String label, Map<String, Object> properties) {
        return DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUid).withProperties(properties).withLabel(label)
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
