/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.vesync.internal.handlers.VeSyncBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link VeSyncDiscoveryService} is an implementation of a discovery service for VeSync devices. The meta-data is
 * read by the bridge, and the discovery data updated via a callback implemented by the DeviceMetaDataUpdatedHandler.
 *
 * @author David Godyear - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.vesync")
public class VeSyncDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService, DeviceMetaDataUpdatedHandler {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);

    private static final int DISCOVER_TIMEOUT_SECONDS = 5;

    private @NonNullByDefault({}) VeSyncBridgeHandler bridgeHandler;
    private @NonNullByDefault({}) ThingUID bridgeUID;

    /**
     * Creates a VeSyncDiscoveryService with enabled autostart.
     */
    public VeSyncDiscoveryService() {
        super(SUPPORTED_THING_TYPES, DISCOVER_TIMEOUT_SECONDS);
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
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof VeSyncBridgeHandler) {
            bridgeHandler = (VeSyncBridgeHandler) handler;
            bridgeUID = bridgeHandler.getUID();
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    protected void startBackgroundDiscovery() {
        if (bridgeHandler != null) {
            bridgeHandler.registerMetaDataUpdatedHandler(this);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        if (bridgeHandler != null) {
            bridgeHandler.unregisterMetaDataUpdatedHandler(this);
        }
    }

    @Override
    protected void startScan() {
        // If the bridge is not online no other thing devices can be found, so no reason to scan at this moment.
        removeOlderResults(getTimestampOfLastScan());
        if (ThingStatus.ONLINE.equals(bridgeHandler.getThing().getStatus())) {
            bridgeHandler.runDeviceScanSequenceNoAuthErrors();
        }
    }

    @Override
    public void handleMetadataRetrieved(VeSyncBridgeHandler handler) {
        bridgeHandler.getAirPurifiersMetadata().map(apMeta -> {
            final Map<String, Object> properties = new HashMap<>(6);
            final String deviceUUID = apMeta.getUuid();
            properties.put(DEVICE_PROP_DEVICE_NAME, apMeta.getDeviceName());
            properties.put(DEVICE_PROP_DEVICE_TYPE, apMeta.getDeviceType());
            properties.put(DEVICE_PROP_DEVICE_MAC_ID, apMeta.getMacId());
            properties.put(DEVICE_PROP_DEVICE_UUID, deviceUUID);
            properties.put(DEVICE_PROP_CONFIG_DEVICE_MAC, apMeta.getMacId());
            properties.put(DEVICE_PROP_CONFIG_DEVICE_NAME, apMeta.getDeviceName());
            return DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_AIR_PURIFIER, bridgeUID, deviceUUID))
                    .withLabel(apMeta.getDeviceName()).withBridge(bridgeUID).withProperties(properties)
                    .withRepresentationProperty(DEVICE_PROP_DEVICE_MAC_ID).build();
        }).forEach(this::thingDiscovered);

        bridgeHandler.getAirHumidifiersMetadata().map(apMeta -> {
            final Map<String, Object> properties = new HashMap<>(6);
            final String deviceUUID = apMeta.getUuid();
            properties.put(DEVICE_PROP_DEVICE_NAME, apMeta.getDeviceName());
            properties.put(DEVICE_PROP_DEVICE_TYPE, apMeta.getDeviceType());
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
