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
package org.openhab.binding.nobohub.internal.discovery;

import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.AUTODISCOVERED_THING_TYPES_UIDS;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.PROPERTY_MODEL;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.PROPERTY_NAME;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.PROPERTY_TEMPERATURE_SENSOR_FOR_ZONE;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.PROPERTY_VENDOR_NAME;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.PROPERTY_ZONE;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.PROPERTY_ZONE_ID;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.THING_TYPE_COMPONENT;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.THING_TYPE_ZONE;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nobohub.internal.NoboHubBridgeHandler;
import org.openhab.binding.nobohub.internal.model.Component;
import org.openhab.binding.nobohub.internal.model.Zone;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class identifies devices that are available on the Nobø hub and adds discovery results for them.
 *
 * @author Jørgen Austvik - Initial contribution
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public class NoboThingDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(NoboThingDiscoveryService.class);

    private final NoboHubBridgeHandler bridgeHandler;

    public NoboThingDiscoveryService(NoboHubBridgeHandler bridgeHandler) {
        super(AUTODISCOVERED_THING_TYPES_UIDS, 10, true);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected void startScan() {
        bridgeHandler.startScan();
    }

    @Override
    public synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime());
    }

    public void detectZones(Collection<Zone> zones) {
        ThingUID bridge = bridgeHandler.getThing().getUID();
        List<Thing> things = bridgeHandler.getThing().getThings();

        for (Zone zone : zones) {
            ThingUID discoveredThingId = new ThingUID(THING_TYPE_ZONE, bridge, Integer.toString(zone.getId()));

            boolean addDiscoveredZone = true;
            for (Thing thing : things) {
                if (thing.getUID().equals(discoveredThingId)) {
                    addDiscoveredZone = false;
                }
            }

            if (addDiscoveredZone) {
                String label = zone.getName();

                Map<String, Object> properties = new HashMap<>(3);
                properties.put(PROPERTY_ZONE_ID, Integer.toString(zone.getId()));
                properties.put(PROPERTY_NAME, zone.getName());
                properties.put(Thing.PROPERTY_VENDOR, PROPERTY_VENDOR_NAME);

                logger.debug("Adding device {} to inbox", discoveredThingId);
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(discoveredThingId).withBridge(bridge)
                        .withLabel(label).withProperties(properties).withRepresentationProperty("id").build();
                thingDiscovered(discoveryResult);
            }
        }
    }

    public void detectComponents(Collection<Component> components) {
        ThingUID bridge = bridgeHandler.getThing().getUID();
        List<Thing> things = bridgeHandler.getThing().getThings();

        for (Component component : components) {
            ThingUID discoveredThingId = new ThingUID(THING_TYPE_COMPONENT, bridge,
                    component.getSerialNumber().toString());

            boolean addDiscoveredComponent = true;
            for (Thing thing : things) {
                if (thing.getUID().equals(discoveredThingId)) {
                    addDiscoveredComponent = false;
                }
            }

            if (addDiscoveredComponent) {
                String label = component.getName();

                Map<String, Object> properties = new HashMap<>(4);
                properties.put(Thing.PROPERTY_SERIAL_NUMBER, component.getSerialNumber().toString());
                properties.put(PROPERTY_NAME, component.getName());
                properties.put(Thing.PROPERTY_VENDOR, PROPERTY_VENDOR_NAME);
                properties.put(PROPERTY_MODEL, component.getSerialNumber().getComponentType());

                String zoneName = getZoneName(component.getZoneId());
                if (zoneName != null) {
                    properties.put(PROPERTY_ZONE, zoneName);
                }

                int zoneId = component.getTemperatureSensorForZoneId();
                if (zoneId >= 0) {
                    String tempForZoneName = getZoneName(zoneId);
                    if (tempForZoneName != null) {
                        properties.put(PROPERTY_TEMPERATURE_SENSOR_FOR_ZONE, tempForZoneName);
                    }
                }

                logger.debug("Adding device {} to inbox", discoveredThingId);
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(discoveredThingId).withBridge(bridge)
                        .withLabel(label).withProperties(properties)
                        .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).build();
                thingDiscovered(discoveryResult);
            }
        }
    }

    private @Nullable String getZoneName(int zoneId) {
        Zone zone = bridgeHandler.getZone(zoneId);
        if (null == zone) {
            return null;
        }

        return zone.getName();
    }
}
