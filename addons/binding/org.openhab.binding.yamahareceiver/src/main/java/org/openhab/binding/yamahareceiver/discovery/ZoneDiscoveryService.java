/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.discovery;

import java.lang.ref.WeakReference;
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
import org.openhab.binding.yamahareceiver.internal.protocol.SystemControl;
import org.openhab.binding.yamahareceiver.internal.protocol.ZoneControl.Zone;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 /**
 *
 * @author David Gräff - Initial contribution
 */
public class ZoneDiscoveryService extends AbstractDiscoveryService {
    private Logger logger = LoggerFactory.getLogger(ZoneDiscoveryService.class);
    private ServiceRegistration<?> reg = null;
    private final WeakReference<SystemControl.State> stateRef;
    private final ThingUID bridgeUid;

    public ZoneDiscoveryService(SystemControl.State state, ThingUID bridgeUid) {
        super(YamahaReceiverBindingConstants.ZONE_THING_TYPES_UIDS, 2, true);
        stateRef = new WeakReference<SystemControl.State>(state);
        this.bridgeUid = bridgeUid;
    }

    public void stop() {
        if (reg != null) {
            reg.unregister();
        }
        reg = null;
    }

    @Override
    protected void startScan() {
        detectZones();
    }

    public void detectZones() {
        SystemControl.State info = stateRef.get();
        if (info == null) {
            stop();
            logger.error("Lost state of AVR in zone discovery!");
            return;
        }

        // Create a copy of the list to avoid concurrent modification exceptions, because
        // the state update takes place in another thread
        List<Zone> zoneCopy = new ArrayList<Zone>(info.zones);

        for (Zone zone : zoneCopy) {
            String zoneName = zone.name();
            ThingUID uid = new ThingUID(YamahaReceiverBindingConstants.ZONE_THING_TYPE, bridgeUid, zoneName);

            Map<String, Object> properties = new HashMap<>();
            properties.put(YamahaReceiverBindingConstants.CONFIG_ZONE, zoneName);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel(info.name + " " + zoneName).withBridge(bridgeUid).build();
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
