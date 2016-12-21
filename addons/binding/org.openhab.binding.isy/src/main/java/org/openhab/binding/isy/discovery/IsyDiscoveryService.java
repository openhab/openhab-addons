package org.openhab.binding.isy.discovery;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.isy.IsyBindingConstants;
import org.openhab.binding.isy.config.IsyInsteonDeviceConfiguration;
import org.openhab.binding.isy.handler.IsyBridgeHandler;
import org.openhab.binding.isy.internal.InsteonAddress;
import org.openhab.binding.isy.internal.InsteonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.universaldevices.client.NoDeviceException;
import com.universaldevices.device.model.UDNode;

public class IsyDiscoveryService extends AbstractDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(IsyDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 30;
    private IsyBridgeHandler bridgeHandler;

    /**
     * Creates a IsyDiscoveryService.
     */
    public IsyDiscoveryService(IsyBridgeHandler bridgeHandler) {
        super(ImmutableSet.of(new ThingTypeUID(IsyBindingConstants.BINDING_ID, "-")), DISCOVER_TIMEOUT_SECONDS, false);
        this.bridgeHandler = bridgeHandler;
    }

    public void activate() {
        bridgeHandler.registerDiscoveryService(this);
    }

    /**
     * Deactivates the Discovchery Service.
     */
    @Override
    public void deactivate() {
        bridgeHandler.unregisterDiscoveryService();
    }

    @Override
    protected void startScan() {
        logger.debug("startScan called for Isy");
        Map<String, Object> properties = null;
        InsteonClient insteon = this.bridgeHandler.getInsteonClient();
        ThingUID bridgeUID = this.bridgeHandler.getThing().getUID();
        try {
            Hashtable<String, UDNode> nodes = insteon.getNodes();

            Collection<UDNode> theNodes = nodes.values();
            logger.debug("found nodes(#): " + nodes.size());
            for (UDNode node : theNodes) {
                boolean supportedDevice = true;
                String name = node.name;
                String type = node.typeReadable;
                String address = node.address;
                DiscoveryResult discoveryResult;
                ThingUID thingUID = null;
                String thingLabel = name;
                InsteonAddress insteonAddress = new InsteonAddress(address);
                String thingID = insteonAddress.toStringNoDeviceId().replace(" ", "");
                properties = new HashMap<>(0);
                properties.put(IsyInsteonDeviceConfiguration.ADDRESS, insteonAddress.toStringNoDeviceId());
                properties.put(IsyInsteonDeviceConfiguration.NAME, name);

                if ("10.01".equals(type)) {
                    // lets only add when the actual motion sensor...all 3 sensors (motion, batt, dusk/dawn) show up
                    // within this type
                    if (insteonAddress.getDeviceId() == 1) {
                        thingUID = new ThingUID(IsyBindingConstants.MOTION_THING_TYPE, bridgeUID, thingID);
                    }
                } else if ("01.20".equals(type) || "01.2D".equals(type)) {
                    thingUID = new ThingUID(IsyBindingConstants.DIMMER_THING_TYPE, bridgeUID, thingID);
                } else if ("07.00".equals(type)) {
                    if (insteonAddress.getDeviceId() == 1) {
                        thingUID = new ThingUID(IsyBindingConstants.GARAGEDOORKIT_THING_TYPE, bridgeUID, thingID);
                    }
                } else if ("02.2A".equals(type)) {
                    thingUID = new ThingUID(IsyBindingConstants.SWITCH_THING_TYPE, bridgeUID, thingID);
                } else if ("02.09".equals(type) || "01.0E".equals(type) || "02.06".equals(type)
                        || "02.38".equals(type)) {
                    thingUID = new ThingUID(IsyBindingConstants.SWITCH_THING_TYPE, bridgeUID, thingID);

                } else if ("10.08".equals(type)) {
                    // lets only add when the sensor which is 1 is detected.
                    // within this type
                    if (insteonAddress.getDeviceId() == 1) {
                        thingUID = new ThingUID(IsyBindingConstants.LEAKDETECTOR_THING_TYPE, bridgeUID, thingID);
                    }
                } else if ("01.1B".equals(type)) {
                    // lets only add when the sensor which is 1 is detected.
                    // within this type
                    if (insteonAddress.getDeviceId() == 1) {
                        // thingID = "keylinc_6" + address.replace(" ", "");
                        thingUID = new ThingUID(IsyBindingConstants.KEYPAD_LINC_6_THING_TYPE, bridgeUID, thingID);
                    }
                } else if ("01.42".equals(type)) {
                    // lets only add when the sensor which is 1 is detected.
                    // within this type
                    if (insteonAddress.getDeviceId() == 1) {
                        // thingID = address.replace(" ", "");
                        thingUID = new ThingUID(IsyBindingConstants.KEYPAD_LINC_5_THING_TYPE, bridgeUID, thingID);
                    }
                } else if ("00.12".equals(type)) {
                    // lets only add when the sensor which is 1 is detected.
                    // within this type
                    if (insteonAddress.getDeviceId() == 1) {
                        // thingID = address.replace(" ", "");
                        thingUID = new ThingUID(IsyBindingConstants.REMOTELINC_8_THING_TYPE, bridgeUID, thingID);
                    }
                } else if ("02.1F".equals(type)) {
                    // lets only add when the sensor which is 1 is detected.
                    // within this type
                    if (insteonAddress.getDeviceId() == 1) {
                        // thingID = address.replace(" ", "");
                        thingUID = new ThingUID(IsyBindingConstants.INLINELINC_SWITCH_THING_TYPE, bridgeUID, thingID);
                    }
                } else {
                    supportedDevice = false;
                    logger.warn(
                            "Unsupported insteon node, name: " + name + ", type: " + type + ", address: " + address);
                }
                // ("20 7B 38 1".equals(address) || "1F 5 6F 1".equals(address))
                // || "21 5F E5 1".equals(address) || "21 5F CE 1".equals(address) || "28 C1 F3 1".equals(address)
                // || "25 B9 AF 1".equals(address)
                if (supportedDevice && thingUID != null) {
                    discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                            .withProperties(properties).withBridge(bridgeUID).withLabel(thingLabel).build();
                    thingDiscovered(discoveryResult);
                }
            }

        } catch (NoDeviceException e) {
            logger.error("No device exception", e);
        }

    }

}
