/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.unifi.internal.access;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.unifi.internal.access.api.UnifiAccessApiClient;
import org.openhab.binding.unifi.internal.access.dto.Device;
import org.openhab.binding.unifi.internal.access.dto.Door;
import org.openhab.binding.unifi.internal.access.dto.UnifiAccessApiException;
import org.openhab.binding.unifi.internal.access.handler.UnifiAccessBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for UniFi Access Door things.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = UnifiAccessDiscoveryService.class)
@NonNullByDefault
public class UnifiAccessDiscoveryService extends AbstractThingHandlerDiscoveryService<UnifiAccessBridgeHandler> {

    private final Logger logger = LoggerFactory.getLogger(UnifiAccessDiscoveryService.class);

    public UnifiAccessDiscoveryService() {
        super(UnifiAccessBridgeHandler.class,
                Set.of(UnifiAccessBindingConstants.DOOR_THING_TYPE, UnifiAccessBindingConstants.DEVICE_THING_TYPE,
                        UnifiAccessBindingConstants.DOOR_THING_TYPE_LEGACY,
                        UnifiAccessBindingConstants.DEVICE_THING_TYPE_LEGACY),
                30, false);
    }

    /**
     * Remaps a canonical {@code unifi:*} thing type to the binding ID of this Access bridge, so a
     * discovered child stays in the same namespace as its parent (legacy {@code unifiaccess:*}
     * children under a legacy bridge, {@code unifi:*} children under a new bridge).
     */
    private ThingTypeUID forBridge(ThingTypeUID canonical) {
        return new ThingTypeUID(thingHandler.getThing().getThingTypeUID().getBindingId(), canonical.getId());
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof UnifiAccessBridgeHandler childDiscoveryHandler) {
            childDiscoveryHandler.setDiscoveryService(this);
            this.thingHandler = childDiscoveryHandler;
        }
    }

    @Override
    protected void startScan() {
        removeOlderResults(getTimestampOfLastScan());
        final UnifiAccessApiClient client = thingHandler.getApiClient();
        if (client == null) {
            return;
        }
        try {
            List<Door> doors = client.getDoors();
            discoverDoors(doors);
            List<Device> devices = client.getDevices();
            discoverDevices(devices);
        } catch (UnifiAccessApiException e) {
            logger.debug("Error discovering doors: {}", e.getMessage());
        }
    }

    public void discoverDoors(List<Door> doors) {
        ThingTypeUID doorType = forBridge(UnifiAccessBindingConstants.DOOR_THING_TYPE);
        for (Door d : doors) {
            ThingUID uid = new ThingUID(doorType, thingHandler.getThing().getUID(), d.id);
            Map<String, Object> props = Map.of(UnifiAccessBindingConstants.CONFIG_DEVICE_ID, d.id);
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(thingHandler.getThing().getUID())
                    .withThingType(doorType).withProperties(props)
                    .withRepresentationProperty(UnifiAccessBindingConstants.CONFIG_DEVICE_ID)
                    .withLabel("UniFi Door: " + (d.fullName != null ? d.fullName : d.name)).build();
            thingDiscovered(result);
        }
    }

    public void discoverDevices(List<Device> devices) {
        ThingTypeUID deviceType = forBridge(UnifiAccessBindingConstants.DEVICE_THING_TYPE);
        for (Device d : devices) {
            String devId = d.id;
            if (devId == null || d.isHub()) {
                continue;
            }
            ThingUID uid = new ThingUID(deviceType, thingHandler.getThing().getUID(), devId);
            Map<String, Object> props = Map.of(UnifiAccessBindingConstants.CONFIG_DEVICE_ID, devId);
            String label = "UniFi Device: " + (d.type != null ? d.type : "") + " - "
                    + (d.alias != null ? d.alias : (d.name != null ? d.name : devId));
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(thingHandler.getThing().getUID())
                    .withThingType(deviceType).withProperties(props)
                    .withRepresentationProperty(UnifiAccessBindingConstants.CONFIG_DEVICE_ID).withLabel(label).build();
            thingDiscovered(result);
        }
    }
}
