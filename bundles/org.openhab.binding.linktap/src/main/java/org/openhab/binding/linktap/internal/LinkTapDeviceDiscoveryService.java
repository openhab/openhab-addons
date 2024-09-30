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
package org.openhab.binding.linktap.internal;

import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * The {@link LinkTapDeviceDiscoveryService} is an implementation of a discovery service for VeSync devices. The
 * meta-data is
 * read by the bridge, and the discovery data updated via a callback implemented by the DeviceMetaDataUpdatedHandler.
 *
 * @author David Godyear - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = LinkTapDeviceDiscoveryService.class, configurationPid = "discovery.linktap.devices")
public class LinkTapDeviceDiscoveryService extends AbstractThingHandlerDiscoveryService<LinkTapBridgeHandler>
        implements DeviceMetaDataUpdatedHandler {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_GATEWAY);
    private static final int DISCOVER_TIMEOUT_SECONDS = 5;

    private @NonNullByDefault({}) ThingUID bridgeUID;

    /**
     * Creates a VeSyncDiscoveryService with enabled autostart.
     */
    public LinkTapDeviceDiscoveryService() {
        super(LinkTapBridgeHandler.class, SUPPORTED_THING_TYPES, DISCOVER_TIMEOUT_SECONDS);
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
    }

    @Override
    public void handleMetadataRetrieved(final LinkTapBridgeHandler handler) {
        thingHandler.getDiscoveredDevices().map(x -> {
            final Map<String, Object> properties = new HashMap<>(4);
            properties.put(DEVICE_PROP_DEV_ID, x.deviceId);
            properties.put(DEVICE_PROP_DEV_NAME, x.deviceName);
            properties.put(DEVICE_CONFIG_DEV_ID, x.deviceId);
            properties.put(DEVICE_CONFIG_DEV_NAME, x.deviceName);
            properties.put(DEVICE_CONFIG_AUTO_ALERTS_ENABLE, true);
            return DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_DEVICE, bridgeUID, x.deviceId))
                    .withBridge(bridgeUID).withProperties(properties).withLabel(x.deviceName)
                    .withRepresentationProperty(DEVICE_PROP_DEV_ID).build();
        }).forEach(this::thingDiscovered);
    }
}
