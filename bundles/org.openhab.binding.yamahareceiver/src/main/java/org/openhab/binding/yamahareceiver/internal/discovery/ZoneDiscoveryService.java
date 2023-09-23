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
package org.openhab.binding.yamahareceiver.internal.discovery;

import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.*;
import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Configs.CONFIG_ZONE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Zone;
import org.openhab.binding.yamahareceiver.internal.handler.YamahaBridgeHandler;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;

/**
 * After the AVR bridge thing has been added and a connection could be established,
 * the user is presented with the available zones.
 *
 * @author David Gräff - Initial contribution
 * @author Tomasz Maruszak - Introduced config object
 */
@NonNullByDefault
public class ZoneDiscoveryService extends AbstractDiscoveryService implements DiscoveryService, ThingHandlerService {

    private @Nullable YamahaBridgeHandler handler;

    /**
     * Constructs a zone discovery service.
     * Registers this zone discovery service programmatically.
     * Call {@link ZoneDiscoveryService#destroy()} to unregister the service after use.
     */
    public ZoneDiscoveryService() {
        super(ZONE_THING_TYPES_UIDS, 0, false);
    }

    @Override
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startScan() {
    }

    public static ThingUID zoneThing(ThingUID bridgeUid, String zoneName) {
        return new ThingUID(ZONE_THING_TYPE, bridgeUid, zoneName);
    }

    /**
     * The available zones are within the {@link DeviceInformationState}. Will will publish those
     * as things via this discovery service instance.
     *
     * @param state The device information state
     * @param bridgeUid The bridge UID
     */
    public void publishZones(DeviceInformationState state, ThingUID bridgeUid) {
        // Create a copy of the list to avoid concurrent modification exceptions, because
        // the state update takes place in another thread
        List<Zone> zoneCopy = new ArrayList<>(state.zones);

        for (Zone zone : zoneCopy) {
            String zoneName = zone.name();
            ThingUID uid = zoneThing(bridgeUid, zoneName);

            Map<String, Object> properties = new HashMap<>();
            properties.put(CONFIG_ZONE, zoneName);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel(state.name + " " + zoneName).withBridge(bridgeUid)
                    .withRepresentationProperty(CONFIG_ZONE).build();

            thingDiscovered(discoveryResult);
        }
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof YamahaBridgeHandler bridgeHandler) {
            bridgeHandler.setZoneDiscoveryService(this);
            this.handler = bridgeHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
