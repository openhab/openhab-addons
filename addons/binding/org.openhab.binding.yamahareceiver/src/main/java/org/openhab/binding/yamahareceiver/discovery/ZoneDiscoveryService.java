/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.discovery;

import static org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.THING_TYPE_YAMAHAAV;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;
import org.openhab.binding.yamahareceiver.internal.YamahaReceiverState;
import org.openhab.binding.yamahareceiver.internal.protocol.YamahaReceiverCommunication.Zone;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class ZoneDiscoveryService extends AbstractDiscoveryService {

    private ServiceRegistration<?> reg = null;

    public ZoneDiscoveryService() {
        super(Collections.singleton(THING_TYPE_YAMAHAAV), 2, true);
    }

    public void stop() {
        if (reg != null) {
            reg.unregister();
        }
        reg = null;
    }

    @Override
    protected void startScan() {
    }

    public void detectZones(YamahaReceiverState state, String base_udn) {
        Map<String, Object> properties = new HashMap<>(3);
        properties.put((String) YamahaReceiverBindingConstants.CONFIG_HOST_NAME, state.getHost());

        for (Zone zone : state.additional_zones) {
            String zoneName = zone.name();
            ThingUID uid = new ThingUID(YamahaReceiverBindingConstants.THING_TYPE_YAMAHAAV, base_udn + zoneName);

            properties.put((String) YamahaReceiverBindingConstants.CONFIG_ZONE, zoneName);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel(state.name + " " + zoneName).build();
            thingDiscovered(discoveryResult);
        }
    }

    public void start(BundleContext bundleContext) {
        if (reg != null) {
            return;
        }
        reg = bundleContext.registerService(DiscoveryService.class.getName(), this, new Hashtable<String, Object>());
    }
}