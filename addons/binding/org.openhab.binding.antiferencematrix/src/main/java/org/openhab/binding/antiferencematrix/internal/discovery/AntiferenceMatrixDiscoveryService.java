package org.openhab.binding.antiferencematrix.internal.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.antiferencematrix.AntiferenceMatrixBindingConstants;
import org.openhab.binding.antiferencematrix.handler.AntiferenceMatrixBridgeHandler;
import org.openhab.binding.antiferencematrix.internal.model.InputPort;
import org.openhab.binding.antiferencematrix.internal.model.OutputPort;
import org.openhab.binding.antiferencematrix.internal.model.PortList;

public class AntiferenceMatrixDiscoveryService extends AbstractDiscoveryService
        implements AntiferenceMatrixDiscoveryListener {

    private final AntiferenceMatrixBridgeHandler bridgeHandler;
    private boolean backgroundDiscovery = false;

    public AntiferenceMatrixDiscoveryService(AntiferenceMatrixBridgeHandler bridgeHandler) {
        super(AntiferenceMatrixBindingConstants.SUPPORTED_THING_TYPES_UIDS, 10);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected void startScan() {
        // This will set off an update and we will get called back in the listener method
        if (bridgeHandler != null) {
            if (!backgroundDiscovery) {
                bridgeHandler.registerDiscoveryListener(this);
            }
            bridgeHandler.smallUpdate();
            if (!backgroundDiscovery) {
                bridgeHandler.unregisterDiscoveryListener();
            }
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        super.startBackgroundDiscovery();
        backgroundDiscovery = true;
        // For background discovery we just register with the matrix and we will get
        // notified any time an update is done by the matrix
        bridgeHandler.registerDiscoveryListener(this);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        super.stopBackgroundDiscovery();
        bridgeHandler.unregisterDiscoveryListener();
        backgroundDiscovery = false;
    }

    @Override
    public void update(PortList portList) {
        for (OutputPort port : portList.getOutputPorts()) {
            discoverOutputPort(port);
        }
        for (InputPort port : portList.getInputPorts()) {
            discoverInputPort(port);
        }

    }

    private void discoverOutputPort(OutputPort port) {
        int outputId = port.getBay();
        String name = port.getName();
        ThingUID thingUID = findThingUID(AntiferenceMatrixBindingConstants.THING_TYPE_MATRIX_OUTPUT.getId(), name);
        Map<String, Object> properties = new HashMap<>();
        properties.put(AntiferenceMatrixBindingConstants.PROPERTY_OUTPUT_ID, outputId);
        addDiscoveredThing(thingUID, properties, name);

    }

    private void discoverInputPort(InputPort port) {
        int inputId = port.getBay();
        String name = port.getName();
        ThingUID thingUID = findThingUID(AntiferenceMatrixBindingConstants.THING_TYPE_MATRIX_INPUT.getId(), name);
        Map<String, Object> properties = new HashMap<>();
        properties.put(AntiferenceMatrixBindingConstants.PROPERTY_INPUT_ID, inputId);
        addDiscoveredThing(thingUID, properties, name);
    }

    private void addDiscoveredThing(ThingUID thingUID, Map<String, Object> properties, String displayLabel) {
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withBridge(bridgeHandler.getThing().getUID()).withLabel(displayLabel).build();

        thingDiscovered(discoveryResult);
    }

    private ThingUID findThingUID(String thingType, String thingId) throws IllegalArgumentException {
        for (ThingTypeUID supportedThingTypeUID : getSupportedThingTypes()) {
            String uid = supportedThingTypeUID.getId();

            if (uid.equalsIgnoreCase(thingType)) {

                return new ThingUID(supportedThingTypeUID, bridgeHandler.getThing().getUID(),
                        thingId.replaceAll("[^a-zA-Z0-9_]", ""));
            }
        }

        throw new IllegalArgumentException("Unsupported device type discovered: " + thingType);
    }

}
