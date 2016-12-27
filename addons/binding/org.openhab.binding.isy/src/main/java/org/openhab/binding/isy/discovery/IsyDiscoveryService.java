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
    private Map<String, ThingTypeUID> mMapDeviceTypeThingType;

    /**
     * Creates a IsyDiscoveryService.
     */
    public IsyDiscoveryService(IsyBridgeHandler bridgeHandler) {
        super(ImmutableSet.of(new ThingTypeUID(IsyBindingConstants.BINDING_ID, "-")), DISCOVER_TIMEOUT_SECONDS, false);
        this.bridgeHandler = bridgeHandler;
        mMapDeviceTypeThingType = new HashMap<String, ThingTypeUID>();
        mMapDeviceTypeThingType.put("10.01", IsyBindingConstants.MOTION_THING_TYPE);
        mMapDeviceTypeThingType.put("01.20", IsyBindingConstants.DIMMER_THING_TYPE);
        mMapDeviceTypeThingType.put("01.2D", IsyBindingConstants.DIMMER_THING_TYPE);
        mMapDeviceTypeThingType.put("07.00", IsyBindingConstants.GARAGEDOORKIT_THING_TYPE);
        mMapDeviceTypeThingType.put("02.2A", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.09", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("01.0E", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.06", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("10.08", IsyBindingConstants.LEAKDETECTOR_THING_TYPE);
        mMapDeviceTypeThingType.put("01.1B", IsyBindingConstants.KEYPAD_LINC_6_THING_TYPE);
        mMapDeviceTypeThingType.put("01.42", IsyBindingConstants.KEYPAD_LINC_5_THING_TYPE);
        mMapDeviceTypeThingType.put("00.12", IsyBindingConstants.REMOTELINC_8_THING_TYPE);
        mMapDeviceTypeThingType.put("02.1F", IsyBindingConstants.INLINELINC_SWITCH_THING_TYPE);
    }

    public void activate() {
        bridgeHandler.registerDiscoveryService(this);
    }

    /**
     * Deactivates the Discovery Service.
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
            logger.debug("retrieving nodes");
            Hashtable<String, UDNode> nodes = insteon.getNodes();
            Collection<UDNode> theNodes = nodes.values();
            logger.debug("found nodes(#): " + nodes.size());
            for (UDNode node : theNodes) {
                InsteonAddress insteonAddress = new InsteonAddress(node.address);

                properties = new HashMap<>(0);
                properties.put(IsyInsteonDeviceConfiguration.ADDRESS, insteonAddress.toStringNoDeviceId());
                properties.put(IsyInsteonDeviceConfiguration.NAME, node.name);

                if (insteonAddress.getDeviceId() == 1) {
                    ThingTypeUID theThingTypeUid = mMapDeviceTypeThingType.get(node.typeReadable);
                    if (theThingTypeUid != null) {
                        String thingID = node.name.replace(" ", "").replaceAll("\\.", "");
                        ThingUID thingUID = new ThingUID(theThingTypeUid, bridgeUID, thingID);
                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                                .withProperties(properties).withBridge(bridgeUID).withLabel(node.name).build();
                        thingDiscovered(discoveryResult);
                    } else {
                        logger.warn("Unsupported insteon node, name: " + node.name + ", type: " + node.typeReadable
                                + ", address: " + node.address);
                    }
                }
            }

        } catch (

        NoDeviceException e) {
            logger.error("No device exception", e);
        }

    }

}
