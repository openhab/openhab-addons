package org.openhab.binding.supla.internal.discovery;

import com.google.common.collect.ImmutableMap;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.supla.handler.SuplaCloudBridgeHandler;
import org.openhab.binding.supla.internal.di.ApplicationContext;
import org.openhab.binding.supla.internal.supla.entities.SuplaIoDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.openhab.binding.supla.SuplaBindingConstants.*;

public class SuplaDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(SuplaDiscoveryService.class);
    private final SuplaCloudBridgeHandler suplaCloudBridgeHandler;
    private final ApplicationContext applicationContext;

    @SuppressWarnings("ConstantConditions")
    public SuplaDiscoveryService(SuplaCloudBridgeHandler suplaCloudBridgeHandler) {
        super(SUPPORTED_THING_TYPES_UIDS, 10, false);
        this.suplaCloudBridgeHandler = suplaCloudBridgeHandler;
        this.applicationContext = suplaCloudBridgeHandler.getApplicationContext().get();
    }

    private void addSuplaThing(SuplaIoDevice device) {
        addThing(suplaCloudBridgeHandler.getThing().getUID(),
                SUPLA_DEVICE_THING_ID,
                device.getId(),
                buildThingLabel(device),
                buildThingProperties(device));
    }

    private void addThing(ThingUID bridgeUID, String thingType, long thingID, String label, Map<String, Object> properties) {
        logger.trace("Adding new Supla thing: {}", thingID);
        ThingUID thingUID = null;
        // TODO
//        switch (thingType) {
//            case ONE_CHANNEL_RELAY_THING_ID:
//                logger.trace("New {}", ONE_CHANNEL_RELAY_THING_TYPE);
//                thingUID = new ThingUID(ONE_CHANNEL_RELAY_THING_TYPE, bridgeUID, String.valueOf(thingID));
//                break;
//        }

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

    @Override
    protected void startScan() {
        applicationContext.getIoDevicesManager()
                .obtainIoDevices()
                .forEach(this::addSuplaThing);
    }

    private String buildThingLabel(SuplaIoDevice device) {
        final StringBuilder sb = new StringBuilder();

        final String name = device.getName();
        if (isValidString(name)) {
            logger.trace("Using name ad ID for {}", device);
            sb.append(name);
        }
        final String comment = device.getComment();
        if (isValidString(comment)) {
            logger.trace("Using comment ad ID for {}", device);
            sb.append("(").append(comment).append(")");
        }
        final String primaryLabel = sb.toString();
        if (isValidString(primaryLabel)) {
            return primaryLabel;
        } else {
            logger.trace("Using gUID ad ID for {}", device);
            return device.getGuid();
        }
    }

    private boolean isValidString(String string) {
        return string != null && !string.trim().isEmpty();
    }

    private Map<String, Object> buildThingProperties(SuplaIoDevice device) {
        return ImmutableMap.<String, Object>builder()
                .put(SUPLA_IO_DEVICE_ID, (int) device.getId())
                .build();
    }
}
