/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.vesync.internal.discovery;

import static org.openhab.binding.vesync.internal.VeSyncConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.vesync.internal.handlers.VeSyncBaseDeviceHandler;
import org.openhab.binding.vesync.internal.handlers.VeSyncBridgeHandler;
import org.openhab.binding.vesync.internal.handlers.VeSyncDeviceAirHumidifierHandler;
import org.openhab.binding.vesync.internal.handlers.VeSyncDeviceAirPurifierHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * The {@link VeSyncDiscoveryService} is an implementation of a discovery service for VeSync devices. The meta-data is
 * read by the bridge, and the discovery data updated via a callback implemented by the DeviceMetaDataUpdatedHandler.
 *
 * @author David Godyear - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = VeSyncDiscoveryService.class, configurationPid = "discovery.vesync")
public class VeSyncDiscoveryService extends AbstractThingHandlerDiscoveryService<VeSyncBridgeHandler>
        implements DeviceMetaDataUpdatedHandler {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BRIDGE);

    private static final int DISCOVER_TIMEOUT_SECONDS = 5;

    private @NonNullByDefault({}) ThingUID bridgeUID;

    /**
     * Creates a VeSyncDiscoveryService with enabled autostart.
     */
    public VeSyncDiscoveryService() {
        super(VeSyncBridgeHandler.class, SUPPORTED_THING_TYPES, DISCOVER_TIMEOUT_SECONDS);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public void activate() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, Boolean.TRUE);
        super.activate(properties);
    }

    @Override
    public void initialize() {
        bridgeUID = thingHandler.getUID();
        super.initialize();
    }

    @Override
    protected void startBackgroundDiscovery() {
        thingHandler.registerMetaDataUpdatedHandler(this);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        thingHandler.unregisterMetaDataUpdatedHandler(this);
    }

    @Override
    protected void startScan() {
        // If the bridge is not online no other thing devices can be found, so no reason to scan at this moment.
        removeOlderResults(getTimestampOfLastScan());
        if (ThingStatus.ONLINE.equals(thingHandler.getThing().getStatus())) {
            thingHandler.runDeviceScanSequenceNoAuthErrors();
        }
    }

    @Override
    public void handleMetadataRetrieved(VeSyncBridgeHandler handler) {
        thingHandler.getAirPurifiersMetadata().map(apMeta -> {
            final Map<String, Object> properties = new HashMap<>(6);
            final String deviceUUID = apMeta.getUuid();
            properties.put(DEVICE_PROP_DEVICE_NAME, apMeta.getDeviceName());
            properties.put(DEVICE_PROP_DEVICE_TYPE, apMeta.getDeviceType());
            properties.put(DEVICE_PROP_DEVICE_FAMILY,
                    VeSyncBaseDeviceHandler.getDeviceFamilyMetadata(apMeta.getDeviceType(),
                            VeSyncDeviceAirHumidifierHandler.DEV_TYPE_FAMILY_AIR_HUMIDIFIER,
                            VeSyncDeviceAirHumidifierHandler.SUPPORTED_MODEL_FAMILIES));
            properties.put(DEVICE_PROP_DEVICE_MAC_ID, apMeta.getMacId());
            properties.put(DEVICE_PROP_DEVICE_UUID, deviceUUID);
            properties.put(DEVICE_PROP_CONFIG_DEVICE_MAC, apMeta.getMacId());
            properties.put(DEVICE_PROP_CONFIG_DEVICE_NAME, apMeta.getDeviceName());
            return DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_AIR_PURIFIER, bridgeUID, deviceUUID))
                    .withLabel(apMeta.getDeviceName()).withBridge(bridgeUID).withProperties(properties)
                    .withRepresentationProperty(DEVICE_PROP_DEVICE_MAC_ID).build();
        }).forEach(this::thingDiscovered);

        thingHandler.getAirHumidifiersMetadata().map(apMeta -> {
            final Map<String, Object> properties = new HashMap<>(6);
            final String deviceUUID = apMeta.getUuid();
            properties.put(DEVICE_PROP_DEVICE_NAME, apMeta.getDeviceName());
            properties.put(DEVICE_PROP_DEVICE_TYPE, apMeta.getDeviceType());
            properties.put(DEVICE_PROP_DEVICE_FAMILY,
                    VeSyncBaseDeviceHandler.getDeviceFamilyMetadata(apMeta.getDeviceType(),
                            VeSyncDeviceAirPurifierHandler.DEV_TYPE_FAMILY_AIR_PURIFIER,
                            VeSyncDeviceAirPurifierHandler.SUPPORTED_MODEL_FAMILIES));
            properties.put(DEVICE_PROP_DEVICE_MAC_ID, apMeta.getMacId());
            properties.put(DEVICE_PROP_DEVICE_UUID, deviceUUID);
            properties.put(DEVICE_PROP_CONFIG_DEVICE_MAC, apMeta.getMacId());
            properties.put(DEVICE_PROP_CONFIG_DEVICE_NAME, apMeta.getDeviceName());
            return DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_AIR_HUMIDIFIER, bridgeUID, deviceUUID))
                    .withLabel(apMeta.getDeviceName()).withBridge(bridgeUID).withProperties(properties)
                    .withRepresentationProperty(DEVICE_PROP_DEVICE_MAC_ID).build();
        }).forEach(this::thingDiscovered);
    }
}
