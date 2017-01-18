package org.openhab.binding.isy.discovery;

import java.util.HashMap;
import java.util.List;
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
import org.openhab.binding.isy.internal.Node;
import org.openhab.binding.isy.internal.OHIsyClient;
import org.openhab.binding.isy.internal.Program;
import org.openhab.binding.isy.internal.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

public class IsyRestDiscoveryService extends AbstractDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(IsyRestDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 30;
    private IsyBridgeHandler bridgeHandler;
    private Map<String, ThingTypeUID> mMapDeviceTypeThingType;

    /**
     * Creates a IsyDiscoveryService.
     */
    public IsyRestDiscoveryService(IsyBridgeHandler bridgeHandler) {
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
        discoverNodes();
        discoverVariables();
        discoverPrograms();

    }

    private void discoverPrograms() {
        OHIsyClient insteon = this.bridgeHandler.getInsteonClient();
        Map<String, Object> properties = null;
        ThingUID bridgeUID = this.bridgeHandler.getThing().getUID();
        for (Program program : insteon.getPrograms()) {
            logger.debug("discovered program: " + program);
            properties = new HashMap<>(0);
            properties.put(IsyInsteonDeviceConfiguration.ID, program.getId());
            properties.put(IsyInsteonDeviceConfiguration.NAME, program.getName());

            ThingTypeUID theThingTypeUid = IsyBindingConstants.PROGRAM_THING_TYPE;
            String thingID = program.getName().replace(" ", "").replaceAll("\\.", "");
            ThingUID thingUID = new ThingUID(theThingTypeUid, bridgeUID, thingID);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withProperties(properties).withBridge(bridgeUID).withLabel(program.getName()).build();
            thingDiscovered(discoveryResult);
            break;
        }
    }

    private void discoverVariables() {
        // TODO implement
        OHIsyClient insteon = this.bridgeHandler.getInsteonClient();
        Map<String, Object> properties = null;
        ThingUID bridgeUID = this.bridgeHandler.getThing().getUID();
        for (Variable variable : insteon.getVariables()) {
            logger.debug("discovered program: " + variable);
            properties = new HashMap<>(0);
            properties.put(IsyInsteonDeviceConfiguration.ID, variable.id);
            properties.put(IsyInsteonDeviceConfiguration.TYPE, variable.type);

            String typeAsText;
            if ("1".equals(variable.type)) {
                typeAsText = "integer";
            } else if ("2".equals(variable.type)) {
                typeAsText = "state";
            } else {
                throw new IllegalStateException("Invalid type for variable:" + variable.type);
            }
            String variableName = "var_" + typeAsText + "_" + variable.id;
            properties.put(IsyInsteonDeviceConfiguration.NAME, variableName);

            ThingTypeUID theThingTypeUid = IsyBindingConstants.VARIABLE_THING_TYPE;
            String thingID = variable.type + "_" + variable.id;
            ThingUID thingUID = new ThingUID(theThingTypeUid, bridgeUID, thingID);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withProperties(properties).withBridge(bridgeUID).withLabel(variableName).build();
            thingDiscovered(discoveryResult);
        }
    }

    private void discoverNodes() {
        logger.debug("startScan called for Isy");
        Map<String, Object> properties = null;
        OHIsyClient insteon = this.bridgeHandler.getInsteonClient();
        ThingUID bridgeUID = this.bridgeHandler.getThing().getUID();
        logger.debug("retrieving nodes");
        List<Node> nodes = insteon.getNodes();
        logger.debug("found nodes(#): " + nodes.size());
        for (Node node : nodes) {
            InsteonAddress insteonAddress = new InsteonAddress(node.getAddress());

            properties = new HashMap<>(0);
            properties.put(IsyInsteonDeviceConfiguration.ADDRESS, insteonAddress.toStringNoDeviceId());
            properties.put(IsyInsteonDeviceConfiguration.NAME, node.getName());

            if (insteonAddress.getDeviceId() == 1) {
                ThingTypeUID theThingTypeUid = mMapDeviceTypeThingType.get(node.getTypeReadable());
                if (theThingTypeUid != null) {
                    String thingID = node.getName().replace(" ", "").replaceAll("\\.", "");
                    ThingUID thingUID = new ThingUID(theThingTypeUid, bridgeUID, thingID);
                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                            .withProperties(properties).withBridge(bridgeUID).withLabel(node.getName()).build();
                    thingDiscovered(discoveryResult);
                } else {
                    logger.warn("Unsupported insteon node, name: " + node.getName() + ", type: "
                            + node.getTypeReadable() + ", address: " + node.getAddress());
                }
            }
        }
    }

}
