package org.openhab.binding.nest.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.nest.NestBindingConstants;
import org.openhab.binding.nest.handler.NestBridgeHandler;
import org.openhab.binding.nest.internal.NestDeviceAddedListener;
import org.openhab.binding.nest.internal.data.Camera;
import org.openhab.binding.nest.internal.data.SmokeDetector;
import org.openhab.binding.nest.internal.data.Structure;
import org.openhab.binding.nest.internal.data.Thermostat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class NestDiscoveryService extends AbstractDiscoveryService implements NestDeviceAddedListener {
    private final Logger logger = LoggerFactory.getLogger(NestDiscoveryService.class);
    private NestBridgeHandler bridge;
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(
            NestBindingConstants.THING_TYPE_THERMOSTAT, NestBindingConstants.THING_TYPE_SMOKE_DETECTOR,
            NestBindingConstants.THING_TYPE_STRUCTURE, NestBindingConstants.THING_TYPE_CAMERA);

    public NestDiscoveryService(NestBridgeHandler bridge) throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES, 60, true);
        this.bridge = bridge;
    }

    public void activate() {
        bridge.addDeviceAddedListener(this);
    }

    @Override
    public void deactivate() {
        bridge.removeDeviceAddedListener(this);
    }

    @Override
    protected void startScan() {
        this.bridge.startDiscoveryScan();
    }

    @Override
    public void onThermostatAdded(Thermostat thermostat) {
        ThingUID bridgeUID = bridge.getThing().getUID();
        ThingUID thingUID = new ThingUID(NestBindingConstants.THING_TYPE_THERMOSTAT, bridgeUID,
                thermostat.getDeviceId());
        Map<String, Object> properties = new HashMap<>(2);
        properties.put(NestBindingConstants.PROPERTY_ID, thermostat.getDeviceId());
        properties.put(NestBindingConstants.PROPERTY_FIRMWARE_VERSION, thermostat.getSoftwareVersion());
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(NestBindingConstants.THING_TYPE_THERMOSTAT).withLabel(thermostat.getNameLong())
                .withBridge(bridgeUID).withProperties(properties).build();
        thingDiscovered(discoveryResult);
        logger.info("thingDiscovered called for thermostat");
    }

    @Override
    public void onCameraAdded(Camera camera) {
        ThingUID bridgeUID = bridge.getThing().getUID();
        ThingUID thingUID = new ThingUID(NestBindingConstants.THING_TYPE_CAMERA, bridgeUID, camera.getDeviceId());
        Map<String, Object> properties = new HashMap<>(2);
        properties.put(NestBindingConstants.PROPERTY_ID, camera.getDeviceId());
        properties.put(NestBindingConstants.PROPERTY_FIRMWARE_VERSION, camera.getSoftwareVersion());
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(NestBindingConstants.THING_TYPE_CAMERA).withLabel(camera.getNameLong())
                .withBridge(bridgeUID).withProperties(properties).build();
        thingDiscovered(discoveryResult);
        logger.info("thingDiscovered called for camera");
    }

    @Override
    public void onSmokeDetectorAdded(SmokeDetector smoke) {
        ThingUID bridgeUID = bridge.getThing().getUID();
        ThingUID thingUID = new ThingUID(NestBindingConstants.THING_TYPE_SMOKE_DETECTOR, bridgeUID,
                smoke.getDeviceId());
        Map<String, Object> properties = new HashMap<>(2);
        properties.put(NestBindingConstants.PROPERTY_ID, smoke.getDeviceId());
        properties.put(NestBindingConstants.PROPERTY_FIRMWARE_VERSION, smoke.getSoftwareVersion());
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(NestBindingConstants.THING_TYPE_SMOKE_DETECTOR).withLabel(smoke.getNameLong())
                .withBridge(bridgeUID).withProperties(properties).build();
        thingDiscovered(discoveryResult);
        logger.info("thingDiscovered called for smoke detector");
    }

    @Override
    public void onStructureAdded(Structure struct) {
        ThingUID bridgeUID = bridge.getThing().getUID();
        ThingUID thingUID = new ThingUID(NestBindingConstants.THING_TYPE_STRUCTURE, bridgeUID, struct.getStructureId());
        Map<String, Object> properties = new HashMap<>(2);
        properties.put(NestBindingConstants.PROPERTY_ID, struct.getStructureId());
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(NestBindingConstants.THING_TYPE_STRUCTURE).withLabel(struct.getName())
                .withBridge(bridgeUID).withProperties(properties).build();
        thingDiscovered(discoveryResult);
        logger.info("thingDiscovered called for structure");
    }
}
