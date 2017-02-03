package org.openhab.binding.nest.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.nest.NestBindingConstants;
import org.openhab.binding.nest.handler.NestBridgeHandler;
import org.openhab.binding.nest.internal.NestDeviceAddedListener;
import org.openhab.binding.nest.internal.data.Camera;
import org.openhab.binding.nest.internal.data.SmokeDetector;
import org.openhab.binding.nest.internal.data.Thermostat;

public class NestDiscoveryService extends AbstractDiscoveryService implements NestDeviceAddedListener {
    private NestBridgeHandler bridge;

    public NestDiscoveryService(NestBridgeHandler bridge) throws IllegalArgumentException {
        super(60);
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
    }

    @Override
    public void onThermostatAdded(Thermostat thermostat) {
        ThingUID bridgeUID = bridge.getThing().getUID();
        ThingUID thingUID = new ThingUID(NestBindingConstants.THING_TYPE_THERMOSTAT, bridgeUID,
                thermostat.getDeviceId());
        Map<String, Object> properties = new HashMap<>(2);
        properties.put("id", thermostat.getDeviceId());
        properties.put("firmware", thermostat.getSoftwareVersion());
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(NestBindingConstants.THING_TYPE_THERMOSTAT).withLabel(thermostat.getNameLong())
                .withBridge(bridgeUID).withProperties(properties).build();
        thingDiscovered(discoveryResult);
    }

    @Override
    public void onCameraAdded(Camera camera) {
        ThingUID bridgeUID = bridge.getThing().getUID();
        ThingUID thingUID = new ThingUID(NestBindingConstants.THING_TYPE_CAMERA, bridgeUID, camera.getDeviceId());
        Map<String, Object> properties = new HashMap<>(2);
        properties.put("id", camera.getDeviceId());
        properties.put("firmware", camera.getSoftwareVersion());
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(NestBindingConstants.THING_TYPE_CAMERA).withLabel(camera.getNameLong())
                .withBridge(bridgeUID).withProperties(properties).build();
        thingDiscovered(discoveryResult);
    }

    @Override
    public void onSmokeDetectorAdded(SmokeDetector smoke) {
        ThingUID bridgeUID = bridge.getThing().getUID();
        ThingUID thingUID = new ThingUID(NestBindingConstants.THING_TYPE_SMOKE_DETECTOR, bridgeUID,
                smoke.getDeviceId());
        Map<String, Object> properties = new HashMap<>(2);
        properties.put("id", smoke.getDeviceId());
        properties.put("firmware", smoke.getSoftwareVersion());
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(NestBindingConstants.THING_TYPE_SMOKE_DETECTOR).withLabel(smoke.getNameLong())
                .withBridge(bridgeUID).withProperties(properties).build();
        thingDiscovered(discoveryResult);
    }
}
