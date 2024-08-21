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
package org.openhab.binding.yamahareceiver.internal.discovery;

import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.*;
import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Configs.CONFIG_ZONE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Zone;
import org.openhab.binding.yamahareceiver.internal.handler.YamahaBridgeHandler;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * After the AVR bridge thing has been added and a connection could be established,
 * the user is presented with the available zones.
 *
 * @author David Gräff - Initial contribution
 * @author Tomasz Maruszak - Introduced config object
 */
@Component(scope = ServiceScope.PROTOTYPE, service = ZoneDiscoveryService.class)
@NonNullByDefault
public class ZoneDiscoveryService extends AbstractThingHandlerDiscoveryService<YamahaBridgeHandler> {

    /**
     * Constructs a zone discovery service.
     * Registers this zone discovery service programmatically.
     * Call {@link ZoneDiscoveryService#destroy()} to unregister the service after use.
     */
    public ZoneDiscoveryService() {
        super(YamahaBridgeHandler.class, ZONE_THING_TYPES_UIDS, 0, false);
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
    public void initialize() {
        thingHandler.setZoneDiscoveryService(this);
        super.initialize();
    }
}
