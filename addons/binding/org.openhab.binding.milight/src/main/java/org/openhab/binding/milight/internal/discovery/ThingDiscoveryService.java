/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.milight.internal.discovery;

import java.util.Hashtable;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.milight.MilightBindingConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * If a bridge is added or if the user scans manually for things this {@link ThingDiscoveryService}
 * is used to return Milights to the framework. Because we can only detect bridges and not individual
 * bulbs, the bridge calls addDevice() on this class for every supported led.
 *
 * @author David Graeff - Initial contribution
 */
public class ThingDiscoveryService extends AbstractDiscoveryService {

    private ServiceRegistration<?> reg = null;
    private ThingUID bridgeUID;

    public ThingDiscoveryService(ThingUID bridgeUID) {
        super(MilightBindingConstants.SUPPORTED_THING_TYPES_UIDS, 2, true);
        this.bridgeUID = bridgeUID;
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

    public void addDevice(ThingUID uid, String thingName) {
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withLabel(thingName)
                .build();
        thingDiscovered(discoveryResult);
    }

    public void start(BundleContext bundleContext) {
        if (reg != null) {
            return;
        }
        reg = bundleContext.registerService(DiscoveryService.class.getName(), this, new Hashtable<String, Object>());
    }
}
