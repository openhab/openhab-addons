/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.miele.internal.discovery;

import static org.openhab.binding.miele.internal.MieleBindingConstants.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.miele.internal.FullyQualifiedApplianceIdentifier;
import org.openhab.binding.miele.internal.handler.ApplianceStatusListener;
import org.openhab.binding.miele.internal.handler.MieleApplianceHandler;
import org.openhab.binding.miele.internal.handler.MieleBridgeHandler;
import org.openhab.binding.miele.internal.handler.MieleBridgeHandler.DeviceClassObject;
import org.openhab.binding.miele.internal.handler.MieleBridgeHandler.DeviceProperty;
import org.openhab.binding.miele.internal.handler.MieleBridgeHandler.HomeDevice;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MieleApplianceDiscoveryService} tracks appliances that are
 * associated with the Miele@Home gateway
 *
 * @author Karel Goderis - Initial contribution
 * @author Martin Lepsy - Added protocol information in order so support WiFi devices
 * @author Jacob Laursen - Fixed multicast and protocol support (ZigBee/LAN)
 */
public class MieleApplianceDiscoveryService extends AbstractDiscoveryService implements ApplianceStatusListener {

    private final Logger logger = LoggerFactory.getLogger(MieleApplianceDiscoveryService.class);

    private static final int SEARCH_TIME = 60;

    private MieleBridgeHandler mieleBridgeHandler;

    public MieleApplianceDiscoveryService(MieleBridgeHandler mieleBridgeHandler) {
        super(MieleApplianceHandler.SUPPORTED_THING_TYPES, SEARCH_TIME, false);
        this.mieleBridgeHandler = mieleBridgeHandler;
    }

    public void activate() {
        mieleBridgeHandler.registerApplianceStatusListener(this);
    }

    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime());
        mieleBridgeHandler.unregisterApplianceStatusListener(this);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return MieleApplianceHandler.SUPPORTED_THING_TYPES;
    }

    @Override
    public void startScan() {
        List<HomeDevice> appliances = mieleBridgeHandler.getHomeDevices();
        if (appliances != null) {
            for (HomeDevice l : appliances) {
                onApplianceAddedInternal(l);
            }
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void onApplianceAdded(HomeDevice appliance) {
        onApplianceAddedInternal(appliance);
    }

    private void onApplianceAddedInternal(HomeDevice appliance) {
        ThingUID thingUID = getThingUID(appliance);
        if (thingUID != null) {
            ThingUID bridgeUID = mieleBridgeHandler.getThing().getUID();
            Map<String, Object> properties = new HashMap<>(9);

            FullyQualifiedApplianceIdentifier applianceIdentifier = appliance.getApplianceIdentifier();
            properties.put(Thing.PROPERTY_VENDOR, appliance.Vendor);
            properties.put(Thing.PROPERTY_MODEL_ID, appliance.getApplianceModel());
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, appliance.getSerialNumber());
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, appliance.getFirmwareVersion());
            properties.put(PROPERTY_PROTOCOL_ADAPTER, appliance.ProtocolAdapterName);
            properties.put(APPLIANCE_ID, applianceIdentifier.getApplianceId());
            String deviceClass = appliance.getDeviceClass();
            if (deviceClass != null) {
                properties.put(PROPERTY_DEVICE_CLASS, deviceClass);
            }
            String connectionType = appliance.getConnectionType();
            if (connectionType != null) {
                properties.put(PROPERTY_CONNECTION_TYPE, connectionType);
            }
            String connectionBaudRate = appliance.getConnectionBaudRate();
            if (connectionBaudRate != null) {
                properties.put(PROPERTY_CONNECTION_BAUD_RATE, connectionBaudRate);
            }

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withBridge(bridgeUID).withLabel(deviceClass != null ? deviceClass : appliance.getApplianceModel())
                    .withRepresentationProperty(APPLIANCE_ID).build();

            thingDiscovered(discoveryResult);
        } else {
            logger.debug("Discovered an unsupported appliance of vendor '{}' with id {}", appliance.Vendor,
                    appliance.UID);
        }
    }

    @Override
    public void onApplianceRemoved(HomeDevice appliance) {
        ThingUID thingUID = getThingUID(appliance);

        if (thingUID != null) {
            thingRemoved(thingUID);
        }
    }

    @Override
    public void onApplianceStateChanged(FullyQualifiedApplianceIdentifier applianceIdentifier, DeviceClassObject dco) {
        // nothing to do
    }

    @Override
    public void onAppliancePropertyChanged(FullyQualifiedApplianceIdentifier applianceIdentifier, DeviceProperty dp) {
        // nothing to do
    }

    private ThingUID getThingUID(HomeDevice appliance) {
        ThingUID bridgeUID = mieleBridgeHandler.getThing().getUID();
        String modelId = appliance.getDeviceClass();

        if (modelId != null) {
            ThingTypeUID thingTypeUID = getThingTypeUidFromModelId(modelId);

            if (getSupportedThingTypes().contains(thingTypeUID)) {
                ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, appliance.getApplianceIdentifier().getId());
                return thingUID;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private ThingTypeUID getThingTypeUidFromModelId(String modelId) {
        /*
         * Coffee machine CVA 6805 is reported as CoffeeSystem, but thing type is
         * coffeemachine. At least until it is known if any models are actually reported
         * as CoffeeMachine, we need this special mapping.
         */
        if (MIELE_DEVICE_CLASS_COFFEE_SYSTEM.equals(modelId)) {
            return THING_TYPE_COFFEEMACHINE;
        }

        String thingTypeId = modelId.replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase();

        return new ThingTypeUID(BINDING_ID, thingTypeId);
    }
}
