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
package org.openhab.binding.evohome.internal.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.evohome.internal.EvohomeBindingConstants;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.Gateway;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.Location;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.Locations;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.TemperatureControlSystem;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.Zone;
import org.openhab.binding.evohome.internal.handler.AccountStatusListener;
import org.openhab.binding.evohome.internal.handler.EvohomeAccountBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EvohomeDiscoveryService} class is capable of discovering the available data from Evohome
 *
 * @author Neil Renaud - Initial contribution
 * @author Jasper van Zuijlen - Background discovery
 *
 */
@NonNullByDefault
public class EvohomeDiscoveryService extends AbstractDiscoveryService implements AccountStatusListener {
    private final Logger logger = LoggerFactory.getLogger(EvohomeDiscoveryService.class);
    private static final int TIMEOUT = 5;

    private EvohomeAccountBridgeHandler bridge;
    private ThingUID bridgeUID;

    public EvohomeDiscoveryService(EvohomeAccountBridgeHandler bridge) {
        super(EvohomeBindingConstants.SUPPORTED_THING_TYPES_UIDS, TIMEOUT);

        this.bridge = bridge;
        this.bridgeUID = this.bridge.getThing().getUID();
        this.bridge.addAccountStatusListener(this);
    }

    @Override
    protected void startScan() {
        discoverDevices();
    }

    @Override
    protected void startBackgroundDiscovery() {
        discoverDevices();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void accountStatusChanged(ThingStatus status) {
        if (status == ThingStatus.ONLINE) {
            discoverDevices();
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        bridge.removeAccountStatusListener(this);
    }

    private void discoverDevices() {
        if (bridge.getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Evohome Gateway not online, scanning postponed");
            return;
        }
        Locations localEvohomeConfig = bridge.getEvohomeConfig();

        if (localEvohomeConfig == null) {
            return;
        }
        for (Location location : localEvohomeConfig) {
            if (location == null) {
                continue;
            }
            for (Gateway gateway : location.getGateways()) {
                for (TemperatureControlSystem tcs : gateway.getTemperatureControlSystems()) {
                    if (tcs == null) {
                        continue;
                    }
                    addDisplayDiscoveryResult(location, tcs);
                    for (Zone zone : tcs.getZones()) {
                        if (zone != null) {
                            addZoneDiscoveryResult(location, zone);
                        }
                    }
                }
            }
        }
        stopScan();
    }

    private void addDisplayDiscoveryResult(Location location, TemperatureControlSystem tcs) {
        String id = tcs.getSystemId();
        String name = location.getLocationInfo().getName();
        ThingUID thingUID = new ThingUID(EvohomeBindingConstants.THING_TYPE_EVOHOME_DISPLAY, bridgeUID, id);

        Map<String, Object> properties = new HashMap<>(2);
        properties.put(EvohomeBindingConstants.PROPERTY_ID, id);
        properties.put(EvohomeBindingConstants.PROPERTY_NAME, name);

        addDiscoveredThing(thingUID, properties, name);
    }

    private void addZoneDiscoveryResult(Location location, Zone zone) {
        String id = zone.getZoneId();
        String name = zone.getName() + " (" + location.getLocationInfo().getName() + ")";
        ThingUID thingUID = new ThingUID(EvohomeBindingConstants.THING_TYPE_EVOHOME_HEATING_ZONE, bridgeUID, id);

        Map<String, Object> properties = new HashMap<>(2);
        properties.put(EvohomeBindingConstants.PROPERTY_ID, id);
        properties.put(EvohomeBindingConstants.PROPERTY_NAME, name);

        addDiscoveredThing(thingUID, properties, name);
    }

    private void addDiscoveredThing(ThingUID thingUID, Map<String, Object> properties, String displayLabel) {
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withBridge(bridgeUID).withLabel(displayLabel).build();
        thingDiscovered(discoveryResult);
    }
}
