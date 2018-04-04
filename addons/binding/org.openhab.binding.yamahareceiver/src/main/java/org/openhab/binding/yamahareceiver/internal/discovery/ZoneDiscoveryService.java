/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.discovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * After the AVR bridge thing has been added and a connection could be established,
 * the user is presented with the available zones.
 *
 * @author David Gräff - Initial contribution
 */
public class ZoneDiscoveryService extends AbstractDiscoveryService {
    private final ServiceRegistration<?> reg;

    /**
     * Constructs a zone discovery service.
     * Registers this zone discovery service programmatically.
     * Call {@link ZoneDiscoveryService.destroy()} to unregister the service after use.
     */
    public ZoneDiscoveryService(BundleContext bundleContext) {
        super(YamahaReceiverBindingConstants.ZONE_THING_TYPES_UIDS, 0, false);
        // Allow bundleContext to be null for the test suite
        if (bundleContext != null) {
            reg = bundleContext.registerService(DiscoveryService.class.getName(), this,
                    new Hashtable<String, Object>());
        } else {
            reg = null;
        }
    }

    /**
     * Unregisters this service from the OSGi service registry.
     * This object cannot be used aynmore after calling this method.
     */
    public void destroy() {
        if (reg != null) {
            reg.unregister();
        }
    }

    @Override
    protected void startScan() {
    }

    public static ThingUID zoneThing(ThingUID bridgeUid, String zoneName) {
        return new ThingUID(YamahaReceiverBindingConstants.ZONE_THING_TYPE, bridgeUid, zoneName);
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
        List<YamahaReceiverBindingConstants.Zone> zoneCopy = new ArrayList<YamahaReceiverBindingConstants.Zone>(
                state.zones);

        for (YamahaReceiverBindingConstants.Zone zone : zoneCopy) {
            String zoneName = zone.name();
            ThingUID uid = zoneThing(bridgeUid, zoneName);

            Map<String, Object> properties = new HashMap<>();
            properties.put(YamahaReceiverBindingConstants.CONFIG_ZONE, zoneName);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel(state.name + " " + zoneName).withBridge(bridgeUid).build();
            thingDiscovered(discoveryResult);
        }
    }
}
