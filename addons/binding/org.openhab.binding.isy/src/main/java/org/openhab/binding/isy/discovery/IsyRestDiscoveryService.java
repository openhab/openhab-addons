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
import org.openhab.binding.isy.config.IsyProgramConfiguration;
import org.openhab.binding.isy.config.IsyVariableConfiguration;
import org.openhab.binding.isy.handler.IsyBridgeHandler;
import org.openhab.binding.isy.internal.InsteonAddress;
import org.openhab.binding.isy.internal.Node;
import org.openhab.binding.isy.internal.OHIsyClient;
import org.openhab.binding.isy.internal.Program;
import org.openhab.binding.isy.internal.Scene;
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
        mMapDeviceTypeThingType.put("01.0E", IsyBindingConstants.DIMMER_THING_TYPE);
        mMapDeviceTypeThingType.put("01.19", IsyBindingConstants.DIMMER_THING_TYPE);
        mMapDeviceTypeThingType.put("21.12", IsyBindingConstants.DIMMER_THING_TYPE);
        mMapDeviceTypeThingType.put("07.00", IsyBindingConstants.GARAGEDOORKIT_THING_TYPE);
        mMapDeviceTypeThingType.put("10.02", IsyBindingConstants.TRIGGERLINC_THING_TYPE);
        mMapDeviceTypeThingType.put("02.2A", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.1C", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.09", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("01.0E", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.06", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.37", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.08", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.38", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("10.08", IsyBindingConstants.LEAKDETECTOR_THING_TYPE);
        mMapDeviceTypeThingType.put("01.1B", IsyBindingConstants.KEYPAD_LINC_6_THING_TYPE);
        mMapDeviceTypeThingType.put("01.41", IsyBindingConstants.KEYPADLINC_8_THING_TYPE);
        mMapDeviceTypeThingType.put("01.42", IsyBindingConstants.KEYPAD_LINC_5_THING_TYPE);
        mMapDeviceTypeThingType.put("00.12", IsyBindingConstants.REMOTELINC_8_THING_TYPE);
        mMapDeviceTypeThingType.put("01.1C", IsyBindingConstants.KEYPADLINC_8_THING_TYPE);
        mMapDeviceTypeThingType.put("01.41", IsyBindingConstants.KEYPADLINC_8_THING_TYPE);
        mMapDeviceTypeThingType.put("02.1F", IsyBindingConstants.INLINELINC_SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.08", IsyBindingConstants.OUTLETLINC_DIMMER_THING_TYPE);

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
        try {
            discoverScenes();
        } catch (Exception e) {
            logger.error("error in discover scenes", e);
        }
        try {
            discoverNodes();
        } catch (Exception e) {
            logger.error("error in discover nodes", e);
        }
        try {
            discoverVariables();
        } catch (Exception e) {
            logger.error("error in discover variables", e);
        }
        try {
            discoverPrograms();
        } catch (Exception e) {
            logger.error("error in discover programs", e);
        }

    }

    private void discoverPrograms() {
        OHIsyClient insteon = this.bridgeHandler.getInsteonClient();
        Map<String, Object> properties = null;
        ThingUID bridgeUID = this.bridgeHandler.getThing().getUID();
        for (Program program : insteon.getPrograms()) {
            logger.debug("discovered program: " + program);
            properties = new HashMap<>(0);
            properties.put(IsyProgramConfiguration.ID, program.getId());
            properties.put(IsyProgramConfiguration.NAME, program.getName());

            ThingTypeUID theThingTypeUid = IsyBindingConstants.PROGRAM_THING_TYPE;
            String thingID = removeInvalidUidChars(program.getName());
            ThingUID thingUID = new ThingUID(theThingTypeUid, bridgeUID, thingID);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withProperties(properties).withBridge(bridgeUID).withLabel(program.getName()).build();
            thingDiscovered(discoveryResult);

            // TODO remove
            // logger.warn("Only discovering 1 program per scan for now, until more program functionality exists");
        }
    }

    private static String removeInvalidUidChars(String original) {
        return original.replace(" ", "").replaceAll("\\.", "").replace(",", "_").replaceAll("-", "_");
    }

    private void discoverScenes() {
        OHIsyClient insteon = this.bridgeHandler.getInsteonClient();
        Map<String, Object> properties = null;
        ThingUID bridgeUID = this.bridgeHandler.getThing().getUID();
        for (Scene scene : insteon.getScenes()) {
            logger.debug("discovered scene: " + scene);
            properties = new HashMap<>(0);
            properties.put(IsyInsteonDeviceConfiguration.ADDRESS, scene.address);
            properties.put(IsyInsteonDeviceConfiguration.NAME, scene.name);

            ThingTypeUID theThingTypeUid = IsyBindingConstants.SCENE_THING_TYPE;
            String thingID = removeInvalidUidChars(scene.name);
            ThingUID thingUID = new ThingUID(theThingTypeUid, bridgeUID, thingID);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withProperties(properties).withBridge(bridgeUID).withLabel(scene.name).build();
            thingDiscovered(discoveryResult);
        }
    }

    private void discoverVariables() {
        OHIsyClient insteon = this.bridgeHandler.getInsteonClient();
        Map<String, Object> properties = null;
        ThingUID bridgeUID = this.bridgeHandler.getThing().getUID();
        for (Variable variable : insteon.getVariables()) {
            logger.debug("discovered program: " + variable);
            properties = new HashMap<>(0);
            properties.put(IsyVariableConfiguration.ID, variable.id);
            properties.put(IsyVariableConfiguration.TYPE, variable.type);

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
            properties.put(IsyInsteonDeviceConfiguration.DEVICEID, node.getTypeReadable());

            if (insteonAddress.getDeviceId() == 1) {
                ThingTypeUID theThingTypeUid = mMapDeviceTypeThingType.get(node.getTypeReadable());
                if (theThingTypeUid == null) {
                    logger.warn("Unsupported insteon node, name: " + node.getName() + ", type: "
                            + node.getTypeReadable() + ", address: " + node.getAddress());
                    theThingTypeUid = IsyBindingConstants.UNRECOGNIZED_SWITCH_THING_TYPE;
                }

                String thingID = removeInvalidUidChars(node.getName());
                ThingUID thingUID = new ThingUID(theThingTypeUid, bridgeUID, thingID);
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                        .withProperties(properties).withBridge(bridgeUID).withLabel(node.getName()).build();
                thingDiscovered(discoveryResult);

            }
        }
    }

}
